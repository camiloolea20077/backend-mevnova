-- ============================================================
-- FASE 2 — Sprint 8 — Módulo: Farmacia e Inventario
-- Script : 13_fase2_farmacia.sql
-- Tablas  : bodega, proveedor, compra, lote, detalle_compra,
--           stock_lote, movimiento_inventario,
--           solicitud_medicamento, detalle_solicitud_medicamento,
--           dispensacion, detalle_dispensacion,
--           ajuste_inventario, detalle_ajuste_inventario
-- Alto tráfico: stock_lote, movimiento_inventario, dispensacion
-- ============================================================

-- ──────────────────────────────────────────────────────────
-- 1. bodega
--    Ubicación física de almacenamiento dentro de una sede.
--    Operativa: empresa_id + sede_id.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bodega (
    id                   serial PRIMARY KEY,
    empresa_id           integer      NOT NULL REFERENCES empresa(id),
    sede_id              integer      NOT NULL REFERENCES sede(id),
    codigo               varchar(20)  NOT NULL,
    nombre               varchar(200) NOT NULL,
    tipo_bodega          varchar(30)  NOT NULL CHECK (tipo_bodega IN (
                             'FARMACIA_CENTRAL','BOTIQUIN_PISO','QUIROFANO',
                             'URGENCIAS','CONSULTORIO','CARRO_PARO','OTRA')),
    responsable_id       integer      REFERENCES profesional_salud(id),
    ubicacion_fisica     varchar(200),
    es_principal         boolean      NOT NULL DEFAULT false,
    permite_dispensar    boolean      NOT NULL DEFAULT true,
    permite_recibir      boolean      NOT NULL DEFAULT true,
    observaciones        text,
    activo               boolean      NOT NULL DEFAULT true,
    created_at           timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT uk_bodega_sede_codigo UNIQUE (sede_id, codigo)
);

COMMENT ON TABLE bodega IS
    'Ubicación física de almacenamiento (farmacia central, botiquín de piso, quirófano, etc.) dentro de una sede.';

-- ──────────────────────────────────────────────────────────
-- 2. proveedor
--    Especialización de tercero como proveedor de medicamentos.
--    Transaccional: empresa_id obligatorio.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS proveedor (
    id                      serial PRIMARY KEY,
    empresa_id              integer      NOT NULL REFERENCES empresa(id),
    tercero_id              integer      NOT NULL REFERENCES tercero(id),
    codigo                  varchar(30)  NOT NULL,
    cuenta_contable         varchar(30),
    plazo_pago_dias         integer      DEFAULT 30,
    descuento_pronto_pago   numeric(5,2) DEFAULT 0,
    requiere_orden_compra   boolean      NOT NULL DEFAULT true,
    observaciones           text,
    activo                  boolean      NOT NULL DEFAULT true,
    created_at              timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at              timestamp,
    deleted_at              timestamp,
    usuario_creacion        integer,
    usuario_modificacion    integer,
    CONSTRAINT uk_proveedor_codigo   UNIQUE (empresa_id, codigo),
    CONSTRAINT uk_proveedor_tercero  UNIQUE (empresa_id, tercero_id),
    CONSTRAINT chk_proveedor_plazo   CHECK (plazo_pago_dias >= 0),
    CONSTRAINT chk_proveedor_descuento CHECK (descuento_pronto_pago >= 0 AND descuento_pronto_pago <= 100)
);

COMMENT ON TABLE proveedor IS
    'Especialización de tercero como proveedor de medicamentos e insumos. Requiere un tercero tipo PROVEEDOR de fase 1.';

-- ──────────────────────────────────────────────────────────
-- 3. lote
--    Lote específico de un producto con fecha de vencimiento.
--    Transaccional por empresa (trazabilidad INVIMA).
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS lote (
    id                   serial PRIMARY KEY,
    empresa_id           integer     NOT NULL REFERENCES empresa(id),
    servicio_salud_id    integer     NOT NULL REFERENCES servicio_salud(id),
    numero_lote          varchar(50) NOT NULL,
    fecha_fabricacion    date,
    fecha_vencimiento    date        NOT NULL,
    registro_invima      varchar(50),
    proveedor_id         integer     REFERENCES proveedor(id),
    observaciones        text,
    activo               boolean     NOT NULL DEFAULT true,
    created_at           timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT uk_lote UNIQUE (empresa_id, servicio_salud_id, numero_lote),
    CONSTRAINT chk_lote_fechas CHECK (
        fecha_fabricacion IS NULL OR fecha_fabricacion <= fecha_vencimiento
    )
);

COMMENT ON TABLE lote IS
    'Lote de un medicamento o insumo con trazabilidad INVIMA y control de vencimiento (FEFO).';

-- ──────────────────────────────────────────────────────────
-- 4. compra
--    Recepción de mercancía desde un proveedor.
--    Operativa: empresa_id + sede_id.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS compra (
    id                         serial PRIMARY KEY,
    empresa_id                 integer       NOT NULL REFERENCES empresa(id),
    sede_id                    integer       NOT NULL REFERENCES sede(id),
    bodega_id                  integer       NOT NULL REFERENCES bodega(id),
    proveedor_id               integer       NOT NULL REFERENCES proveedor(id),
    numero_compra              varchar(30)   NOT NULL,
    numero_factura_proveedor   varchar(50),
    fecha_compra               date          NOT NULL,
    fecha_recepcion            date,
    estado_compra              varchar(20)   NOT NULL DEFAULT 'BORRADOR' CHECK (estado_compra IN (
                                   'BORRADOR','RECIBIDA','PAGADA','ANULADA')),
    subtotal                   numeric(18,2) NOT NULL DEFAULT 0,
    total_iva                  numeric(18,2) NOT NULL DEFAULT 0,
    total_descuento            numeric(18,2) NOT NULL DEFAULT 0,
    total                      numeric(18,2) NOT NULL DEFAULT 0,
    soporte_url                varchar(500),
    observaciones              text,
    activo                     boolean       NOT NULL DEFAULT true,
    created_at                 timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at                 timestamp,
    deleted_at                 timestamp,
    usuario_creacion           integer,
    usuario_modificacion       integer,
    CONSTRAINT uk_compra_numero  UNIQUE (empresa_id, numero_compra),
    CONSTRAINT chk_compra_totales CHECK (
        subtotal >= 0 AND total_iva >= 0 AND total_descuento >= 0 AND total >= 0
    )
);

COMMENT ON TABLE compra IS
    'Encabezado de recepción de mercancía desde un proveedor. El detalle de ítems y lotes va en detalle_compra.';

-- ──────────────────────────────────────────────────────────
-- 5. detalle_compra
--    Ítem de compra con lote asociado.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS detalle_compra (
    id                   serial PRIMARY KEY,
    empresa_id           integer       NOT NULL REFERENCES empresa(id),
    compra_id            integer       NOT NULL REFERENCES compra(id),
    servicio_salud_id    integer       NOT NULL REFERENCES servicio_salud(id),
    lote_id              integer       NOT NULL REFERENCES lote(id),
    cantidad             numeric(15,3) NOT NULL CHECK (cantidad > 0),
    valor_unitario       numeric(15,2) NOT NULL CHECK (valor_unitario >= 0),
    porcentaje_iva       numeric(5,2)  DEFAULT 0,
    valor_iva            numeric(15,2) DEFAULT 0,
    porcentaje_descuento numeric(5,2)  DEFAULT 0,
    valor_descuento      numeric(15,2) DEFAULT 0,
    subtotal             numeric(15,2) NOT NULL,
    total                numeric(15,2) NOT NULL,
    observaciones        varchar(300),
    activo               boolean       NOT NULL DEFAULT true,
    created_at           timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    CONSTRAINT chk_detalle_compra_pct CHECK (
        porcentaje_iva       BETWEEN 0 AND 100 AND
        porcentaje_descuento BETWEEN 0 AND 100
    )
);

COMMENT ON TABLE detalle_compra IS
    'Ítem de compra. Cada fila referencia un lote y un producto (servicio_salud). Al confirmar la compra actualiza stock_lote.';

-- ──────────────────────────────────────────────────────────
-- 6. stock_lote  [ALTO TRÁFICO]
--    Stock disponible por lote y bodega.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stock_lote (
    id                    serial PRIMARY KEY,
    empresa_id            integer       NOT NULL REFERENCES empresa(id),
    sede_id               integer       NOT NULL REFERENCES sede(id),
    bodega_id             integer       NOT NULL REFERENCES bodega(id),
    lote_id               integer       NOT NULL REFERENCES lote(id),
    cantidad_disponible   numeric(15,3) NOT NULL DEFAULT 0,
    cantidad_reservada    numeric(15,3) NOT NULL DEFAULT 0,
    cantidad_total        numeric(15,3) GENERATED ALWAYS AS
                              (cantidad_disponible + cantidad_reservada) STORED,
    ultimo_movimiento_at  timestamp,
    activo                boolean       NOT NULL DEFAULT true,
    created_at            timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at            timestamp,
    deleted_at            timestamp,
    CONSTRAINT uk_stock_lote_bodega        UNIQUE (bodega_id, lote_id),
    CONSTRAINT chk_stock_no_negativo       CHECK (
        cantidad_disponible >= 0 AND cantidad_reservada >= 0
    )
);

COMMENT ON TABLE stock_lote IS
    'Stock disponible por lote y bodega. Fuente de verdad para consultas de disponibilidad. Alto tráfico: actualizado en cada movimiento.';

-- ──────────────────────────────────────────────────────────
-- 7. movimiento_inventario  [ALTO TRÁFICO]
--    Auditoría completa de entradas, salidas, traslados y ajustes.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id                  serial PRIMARY KEY,
    empresa_id          integer       NOT NULL REFERENCES empresa(id),
    sede_id             integer       NOT NULL REFERENCES sede(id),
    tipo_movimiento     varchar(30)   NOT NULL CHECK (tipo_movimiento IN (
                            'ENTRADA_COMPRA','SALIDA_DISPENSACION',
                            'TRASLADO_SALIDA','TRASLADO_ENTRADA',
                            'AJUSTE_POSITIVO','AJUSTE_NEGATIVO',
                            'DEVOLUCION_PACIENTE','DEVOLUCION_PROVEEDOR',
                            'BAJA_VENCIMIENTO','BAJA_DETERIORO')),
    bodega_origen_id    integer       REFERENCES bodega(id),
    bodega_destino_id   integer       REFERENCES bodega(id),
    lote_id             integer       NOT NULL REFERENCES lote(id),
    servicio_salud_id   integer       NOT NULL REFERENCES servicio_salud(id),
    cantidad            numeric(15,3) NOT NULL CHECK (cantidad > 0),
    valor_unitario      numeric(15,2) NOT NULL DEFAULT 0,
    valor_total         numeric(15,2) GENERATED ALWAYS AS (cantidad * valor_unitario) STORED,
    -- referencia al documento origen: tipo ('COMPRA','DISPENSACION','TRASLADO','AJUSTE') + id
    referencia_tipo     varchar(30),
    referencia_id       integer,
    motivo              varchar(300),
    fecha_movimiento    timestamp     NOT NULL DEFAULT current_timestamp,
    activo              boolean       NOT NULL DEFAULT true,
    created_at          timestamp     NOT NULL DEFAULT current_timestamp,
    usuario_creacion    integer,
    deleted_at          timestamp,
    CONSTRAINT chk_bodegas_movimiento CHECK (
        (tipo_movimiento IN (
             'ENTRADA_COMPRA','TRASLADO_ENTRADA',
             'AJUSTE_POSITIVO','DEVOLUCION_PACIENTE')
         AND bodega_destino_id IS NOT NULL)
        OR
        (tipo_movimiento IN (
             'SALIDA_DISPENSACION','TRASLADO_SALIDA','AJUSTE_NEGATIVO',
             'DEVOLUCION_PROVEEDOR','BAJA_VENCIMIENTO','BAJA_DETERIORO')
         AND bodega_origen_id IS NOT NULL)
    )
);

COMMENT ON TABLE movimiento_inventario IS
    'Registro inmutable de todo movimiento de inventario. Fuente para reconstruir el kardex por lote/bodega. No tiene updated_at por diseño: los movimientos no se modifican.';

-- ──────────────────────────────────────────────────────────
-- 8. solicitud_medicamento
--    Pedido de un servicio hacia farmacia central.
--    Operativa: empresa_id + sede_id.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS solicitud_medicamento (
    id                           serial PRIMARY KEY,
    empresa_id                   integer     NOT NULL REFERENCES empresa(id),
    sede_id                      integer     NOT NULL REFERENCES sede(id),
    numero_solicitud             varchar(30) NOT NULL,
    bodega_origen_id             integer     NOT NULL REFERENCES bodega(id),
    bodega_destino_id            integer     NOT NULL REFERENCES bodega(id),
    profesional_solicitante_id   integer     REFERENCES profesional_salud(id),
    estado_solicitud             varchar(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado_solicitud IN (
                                     'PENDIENTE','EN_PROCESO','DESPACHADA',
                                     'PARCIAL','RECHAZADA','ANULADA')),
    prioridad                    varchar(20) NOT NULL DEFAULT 'NORMAL' CHECK (prioridad IN (
                                     'NORMAL','URGENTE','VITAL')),
    fecha_solicitud              timestamp   NOT NULL DEFAULT current_timestamp,
    fecha_despacho               timestamp,
    motivo                       text,
    observaciones                text,
    activo                       boolean     NOT NULL DEFAULT true,
    created_at                   timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at                   timestamp,
    deleted_at                   timestamp,
    usuario_creacion             integer,
    usuario_modificacion         integer,
    CONSTRAINT uk_solicitud_numero UNIQUE (empresa_id, numero_solicitud),
    CONSTRAINT chk_solicitud_bodegas CHECK (bodega_origen_id <> bodega_destino_id)
);

COMMENT ON TABLE solicitud_medicamento IS
    'Pedido de medicamentos e insumos desde un servicio (botiquín/piso) hacia farmacia central o entre bodegas.';

-- ──────────────────────────────────────────────────────────
-- 9. detalle_solicitud_medicamento
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS detalle_solicitud_medicamento (
    id                    serial PRIMARY KEY,
    empresa_id            integer       NOT NULL REFERENCES empresa(id),
    solicitud_id          integer       NOT NULL REFERENCES solicitud_medicamento(id),
    servicio_salud_id     integer       NOT NULL REFERENCES servicio_salud(id),
    cantidad_solicitada   numeric(15,3) NOT NULL CHECK (cantidad_solicitada > 0),
    cantidad_despachada   numeric(15,3) NOT NULL DEFAULT 0,
    estado                varchar(20)   NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN (
                              'PENDIENTE','DESPACHADO','PARCIAL','RECHAZADO')),
    motivo_rechazo        varchar(300),
    activo                boolean       NOT NULL DEFAULT true,
    created_at            timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at            timestamp,
    deleted_at            timestamp,
    CONSTRAINT chk_dsm_cantidad CHECK (
        cantidad_despachada >= 0 AND cantidad_despachada <= cantidad_solicitada
    )
);

COMMENT ON TABLE detalle_solicitud_medicamento IS
    'Ítem de una solicitud de medicamento. Registra cuánto se solicitó vs. cuánto fue despachado.';

-- ──────────────────────────────────────────────────────────
-- 10. dispensacion  [ALTO TRÁFICO]
--     Entrega real al paciente con trazabilidad de lote.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS dispensacion (
    id                           serial PRIMARY KEY,
    empresa_id                   integer     NOT NULL REFERENCES empresa(id),
    sede_id                      integer     NOT NULL REFERENCES sede(id),
    bodega_id                    integer     NOT NULL REFERENCES bodega(id),
    numero_dispensacion          varchar(30) NOT NULL,
    prescripcion_id              integer     REFERENCES prescripcion(id),
    paciente_id                  integer     NOT NULL REFERENCES paciente(id),
    profesional_dispensador_id   integer     NOT NULL REFERENCES profesional_salud(id),
    profesional_receptor_id      integer     REFERENCES profesional_salud(id),
    fecha_dispensacion           timestamp   NOT NULL DEFAULT current_timestamp,
    estado                       varchar(20) NOT NULL DEFAULT 'COMPLETA' CHECK (estado IN (
                                     'COMPLETA','PARCIAL','ANULADA')),
    observaciones                text,
    activo                       boolean     NOT NULL DEFAULT true,
    created_at                   timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at                   timestamp,
    deleted_at                   timestamp,
    usuario_creacion             integer,
    usuario_modificacion         integer,
    CONSTRAINT uk_dispensacion_numero UNIQUE (empresa_id, numero_dispensacion)
);

COMMENT ON TABLE dispensacion IS
    'Encabezado de dispensación al paciente. Cada dispensación reduce stock y genera movimiento_inventario tipo SALIDA_DISPENSACION.';

-- ──────────────────────────────────────────────────────────
-- 11. detalle_dispensacion
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS detalle_dispensacion (
    id                       serial PRIMARY KEY,
    empresa_id               integer       NOT NULL REFERENCES empresa(id),
    dispensacion_id          integer       NOT NULL REFERENCES dispensacion(id),
    detalle_prescripcion_id  integer       REFERENCES detalle_prescripcion(id),
    servicio_salud_id        integer       NOT NULL REFERENCES servicio_salud(id),
    lote_id                  integer       NOT NULL REFERENCES lote(id),
    cantidad                 numeric(15,3) NOT NULL CHECK (cantidad > 0),
    valor_unitario           numeric(15,2),
    observaciones            varchar(300),
    activo                   boolean       NOT NULL DEFAULT true,
    created_at               timestamp     NOT NULL DEFAULT current_timestamp,
    deleted_at               timestamp
);

COMMENT ON TABLE detalle_dispensacion IS
    'Ítem de dispensación con trazabilidad de lote específico (FEFO). Sin updated_at por diseño: los detalles dispensados son inmutables.';

-- ──────────────────────────────────────────────────────────
-- 12. ajuste_inventario
--     Ajustes por inventario físico, deterioro, vencimiento.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ajuste_inventario (
    id                   serial PRIMARY KEY,
    empresa_id           integer       NOT NULL REFERENCES empresa(id),
    sede_id              integer       NOT NULL REFERENCES sede(id),
    bodega_id            integer       NOT NULL REFERENCES bodega(id),
    numero_ajuste        varchar(30)   NOT NULL,
    tipo_ajuste          varchar(30)   NOT NULL CHECK (tipo_ajuste IN (
                             'FISICO','DETERIORO','VENCIMIENTO',
                             'ERROR_CAPTURA','PERDIDA','OTRO')),
    fecha_ajuste         date          NOT NULL,
    motivo               text          NOT NULL,
    valor_total_ajuste   numeric(18,2) NOT NULL DEFAULT 0,
    aprobado_por_id      integer       REFERENCES usuario(id),
    fecha_aprobacion     timestamp,
    estado               varchar(20)   NOT NULL DEFAULT 'BORRADOR' CHECK (estado IN (
                             'BORRADOR','APROBADO','APLICADO','ANULADO')),
    activo               boolean       NOT NULL DEFAULT true,
    created_at           timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT uk_ajuste_numero UNIQUE (empresa_id, numero_ajuste)
);

COMMENT ON TABLE ajuste_inventario IS
    'Encabezado de ajuste de inventario. Solo pasa a APLICADO cuando es APROBADO, generando movimientos_inventario correspondientes.';

-- ──────────────────────────────────────────────────────────
-- 13. detalle_ajuste_inventario
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS detalle_ajuste_inventario (
    id                serial PRIMARY KEY,
    empresa_id        integer       NOT NULL REFERENCES empresa(id),
    ajuste_id         integer       NOT NULL REFERENCES ajuste_inventario(id),
    lote_id           integer       NOT NULL REFERENCES lote(id),
    servicio_salud_id integer       NOT NULL REFERENCES servicio_salud(id),
    cantidad_sistema  numeric(15,3) NOT NULL,
    cantidad_real     numeric(15,3) NOT NULL,
    diferencia        numeric(15,3) GENERATED ALWAYS AS (cantidad_real - cantidad_sistema) STORED,
    valor_unitario    numeric(15,2),
    valor_diferencia  numeric(15,2),
    observaciones     varchar(300),
    activo            boolean       NOT NULL DEFAULT true,
    created_at        timestamp     NOT NULL DEFAULT current_timestamp,
    deleted_at        timestamp
);

COMMENT ON TABLE detalle_ajuste_inventario IS
    'Ítem de ajuste de inventario. diferencia positiva = faltante en sistema, negativa = sobrante.';

-- ──────────────────────────────────────────────────────────
-- Índices
-- ──────────────────────────────────────────────────────────

-- bodega
CREATE INDEX IF NOT EXISTS idx_bodega_sede
    ON bodega (sede_id)
    WHERE deleted_at IS NULL;

-- lote: vencimientos próximos (alertas FEFO)
CREATE INDEX IF NOT EXISTS idx_lote_empresa_vencimiento
    ON lote (empresa_id, fecha_vencimiento)
    WHERE deleted_at IS NULL;

-- stock_lote [ALTO TRÁFICO]: consulta de disponibilidad por bodega
CREATE INDEX IF NOT EXISTS idx_stock_lote_bodega_activo
    ON stock_lote (bodega_id, lote_id)
    WHERE deleted_at IS NULL;

-- stock_lote: consulta de stock por empresa y lote
CREATE INDEX IF NOT EXISTS idx_stock_lote_empresa_lote
    ON stock_lote (empresa_id, lote_id)
    WHERE deleted_at IS NULL;

-- movimiento_inventario [ALTO TRÁFICO]: kardex por lote
CREATE INDEX IF NOT EXISTS idx_movimiento_inventario_lote
    ON movimiento_inventario (empresa_id, lote_id, fecha_movimiento DESC)
    WHERE deleted_at IS NULL;

-- movimiento_inventario: consulta por tipo y fecha para reportes
CREATE INDEX IF NOT EXISTS idx_movimiento_inventario_tipo_fecha
    ON movimiento_inventario (empresa_id, tipo_movimiento, fecha_movimiento DESC)
    WHERE deleted_at IS NULL;

-- dispensacion [ALTO TRÁFICO]: historial por paciente
CREATE INDEX IF NOT EXISTS idx_dispensacion_paciente
    ON dispensacion (empresa_id, paciente_id, fecha_dispensacion DESC)
    WHERE deleted_at IS NULL;

-- dispensacion: trazabilidad por prescripcion
CREATE INDEX IF NOT EXISTS idx_dispensacion_prescripcion
    ON dispensacion (prescripcion_id)
    WHERE prescripcion_id IS NOT NULL AND deleted_at IS NULL;

-- detalle_dispensacion: trazabilidad de lote dispensado
CREATE INDEX IF NOT EXISTS idx_detalle_dispensacion_lote
    ON detalle_dispensacion (empresa_id, lote_id)
    WHERE deleted_at IS NULL;

-- solicitud_medicamento: bandeja de pendientes
CREATE INDEX IF NOT EXISTS idx_solicitud_medicamento_estado
    ON solicitud_medicamento (empresa_id, estado_solicitud)
    WHERE deleted_at IS NULL;

-- ajuste_inventario: flujo de aprobación
CREATE INDEX IF NOT EXISTS idx_ajuste_inventario_estado
    ON ajuste_inventario (empresa_id, estado)
    WHERE deleted_at IS NULL;

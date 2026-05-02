# Modelo Farmacia

> Archivo modular extraído de `agente_modelo_datos_fase2.md`.

## Reglas heredadas de fase 1 (NO NEGOCIABLES)

Toda tabla nueva de fase 2 cumple obligatoriamente:

### 1. Nomenclatura
- Nombres de tabla en **español**, **singular**, **snake_case** (`nota_enfermeria`, no `notas_enfermerias`).
- Columnas en **snake_case** español (`fecha_dispensacion`, `numero_lote`).
- **Excepción de auditoría**: campos de auditoría en inglés (`created_at`, `updated_at`, `deleted_at`) por consistencia con el código Java.

### 2. Multi-tenant obligatorio
- Toda tabla transaccional incluye `empresa_id` (FK a `empresa`, NOT NULL).
- Toda tabla operativa incluye `sede_id` (FK a `sede`).
- Catálogos globales sin `empresa_id` (compartidos entre empresas: motivos de glosa por norma, tipos de movimiento de inventario, etc.).
- Unicidades compuestas con `empresa_id` (ej: `UNIQUE (empresa_id, codigo)`).

### 3. Auditoría estándar en cada tabla
```sql
created_at           timestamp NOT NULL DEFAULT current_timestamp,
updated_at           timestamp,
deleted_at           timestamp,
usuario_creacion     integer,
usuario_modificacion integer,
activo               boolean NOT NULL DEFAULT true,
```

### 4. Soft delete universal
- Nunca borrado físico.
- Toda consulta filtra `WHERE deleted_at IS NULL`.
- Índices parciales `WHERE deleted_at IS NULL` en tablas de alto tráfico.

### 5. Dependencias permitidas
- Tablas fase 2 pueden referenciar tablas fase 1 (FKs directas).
- Tablas fase 1 NO se modifican estructuralmente, salvo por necesidad concreta documentada.
- Catálogos globales fase 1 se reutilizan al máximo (no duplicar `tipo_documento`, `pais`, `municipio`, etc.).

---

---

## Módulo 2: Farmacia e inventario

### Contexto del negocio

Toda institución que dispense medicamentos e insumos debe tener:
- **Trazabilidad por lote**: saber qué lote específico se entregó a qué paciente.
- **Control de vencimientos**: alertas y bloqueos para no dispensar vencidos.
- **Stock por ubicación**: distintas bodegas/botiquines en distintas sedes.
- **FEFO** (First Expired First Out): se despacha primero el lote más cercano a vencer.
- **Movimientos auditados**: entradas, salidas, traslados, ajustes, devoluciones.

### Tablas existentes en fase 1 (se reutilizan)
- `servicio_salud` — define qué es un medicamento o insumo (categoría)
- `prescripcion` y `detalle_prescripcion` — qué se ordenó dispensar
- `tercero` — los proveedores son terceros tipo `PROVEEDOR`

### Tabla nueva 2.1: `bodega`

Ubicación física de almacenamiento dentro de una sede. Una sede puede tener varias bodegas (farmacia central, botiquín de piso, quirófano, etc.).

```sql
CREATE TABLE bodega (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    sede_id              integer NOT NULL REFERENCES sede(id),
    codigo               varchar(20) NOT NULL,
    nombre               varchar(200) NOT NULL,
    tipo_bodega          varchar(30) NOT NULL CHECK (tipo_bodega IN
        ('FARMACIA_CENTRAL','BOTIQUIN_PISO','QUIROFANO','URGENCIAS',
         'CONSULTORIO','CARRO_PARO','OTRA')),
    responsable_id       integer REFERENCES profesional_salud(id),
    ubicacion_fisica     varchar(200),
    es_principal         boolean NOT NULL DEFAULT false,
    permite_dispensar    boolean NOT NULL DEFAULT true,
    permite_recibir      boolean NOT NULL DEFAULT true,
    observaciones        text,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT uk_bodega_sede_codigo UNIQUE (sede_id, codigo)
);
```

### Tabla nueva 2.2: `proveedor`

Especialización de tercero como proveedor de medicamentos/insumos.

```sql
CREATE TABLE proveedor (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    tercero_id           integer NOT NULL REFERENCES tercero(id),
    codigo               varchar(30) NOT NULL,
    cuenta_contable      varchar(30),
    plazo_pago_dias      integer DEFAULT 30,
    descuento_pronto_pago numeric(5,2) DEFAULT 0,
    requiere_orden_compra boolean NOT NULL DEFAULT true,
    observaciones        text,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT uk_proveedor_codigo UNIQUE (empresa_id, codigo),
    CONSTRAINT uk_proveedor_tercero UNIQUE (empresa_id, tercero_id)
);
```

### Tabla nueva 2.3: `compra`

Recepción de mercancía desde un proveedor.

```sql
CREATE TABLE compra (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    sede_id              integer NOT NULL REFERENCES sede(id),
    bodega_id            integer NOT NULL REFERENCES bodega(id),
    proveedor_id         integer NOT NULL REFERENCES proveedor(id),
    numero_compra        varchar(30) NOT NULL,
    numero_factura_proveedor varchar(50),
    fecha_compra         date NOT NULL,
    fecha_recepcion      date,
    estado_compra        varchar(20) NOT NULL DEFAULT 'BORRADOR' CHECK (estado_compra IN
        ('BORRADOR','RECIBIDA','PAGADA','ANULADA')),
    subtotal             numeric(18,2) NOT NULL DEFAULT 0,
    total_iva            numeric(18,2) NOT NULL DEFAULT 0,
    total_descuento      numeric(18,2) NOT NULL DEFAULT 0,
    total                numeric(18,2) NOT NULL DEFAULT 0,
    soporte_url          varchar(500),
    observaciones        text,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT uk_compra_numero UNIQUE (empresa_id, numero_compra)
);
```

### Tabla nueva 2.4: `lote`

Lote específico de un producto. Cada lote tiene fecha de vencimiento y registro INVIMA.

```sql
CREATE TABLE lote (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    servicio_salud_id    integer NOT NULL REFERENCES servicio_salud(id),
    numero_lote          varchar(50) NOT NULL,
    fecha_fabricacion    date,
    fecha_vencimiento    date NOT NULL,
    registro_invima      varchar(50),
    proveedor_id         integer REFERENCES proveedor(id),
    observaciones        text,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT uk_lote UNIQUE (empresa_id, servicio_salud_id, numero_lote)
);
```

### Tabla nueva 2.5: `detalle_compra`

Ítem específico de una compra. Genera un lote nuevo o actualiza stock de uno existente.

```sql
CREATE TABLE detalle_compra (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    compra_id            integer NOT NULL REFERENCES compra(id),
    servicio_salud_id    integer NOT NULL REFERENCES servicio_salud(id),
    lote_id              integer NOT NULL REFERENCES lote(id),
    cantidad             numeric(15,3) NOT NULL CHECK (cantidad > 0),
    valor_unitario       numeric(15,2) NOT NULL CHECK (valor_unitario >= 0),
    porcentaje_iva       numeric(5,2) DEFAULT 0,
    valor_iva            numeric(15,2) DEFAULT 0,
    porcentaje_descuento numeric(5,2) DEFAULT 0,
    valor_descuento      numeric(15,2) DEFAULT 0,
    subtotal             numeric(15,2) NOT NULL,
    total                numeric(15,2) NOT NULL,
    observaciones        varchar(300),
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp
);
```

### Tabla nueva 2.6: `stock_lote`

Stock disponible por lote y bodega. Una fila representa cuánto hay del lote X en la bodega Y.

```sql
CREATE TABLE stock_lote (
    id                    serial PRIMARY KEY,
    empresa_id            integer NOT NULL REFERENCES empresa(id),
    sede_id               integer NOT NULL REFERENCES sede(id),
    bodega_id             integer NOT NULL REFERENCES bodega(id),
    lote_id               integer NOT NULL REFERENCES lote(id),
    cantidad_disponible   numeric(15,3) NOT NULL DEFAULT 0,
    cantidad_reservada    numeric(15,3) NOT NULL DEFAULT 0,
    cantidad_total        numeric(15,3) GENERATED ALWAYS AS
        (cantidad_disponible + cantidad_reservada) STORED,
    ultimo_movimiento_at  timestamp,
    activo                boolean NOT NULL DEFAULT true,
    created_at            timestamp NOT NULL DEFAULT current_timestamp,
    updated_at            timestamp,
    deleted_at            timestamp,
    CONSTRAINT uk_stock_lote_bodega UNIQUE (bodega_id, lote_id),
    CONSTRAINT chk_stock_no_negativo CHECK (cantidad_disponible >= 0 AND cantidad_reservada >= 0)
);
```

### Tabla nueva 2.7: `movimiento_inventario`

Registro de toda entrada, salida, traslado o ajuste de stock. Es la fuente de verdad para reconstruir el kardex.

```sql
CREATE TABLE movimiento_inventario (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    sede_id                  integer NOT NULL REFERENCES sede(id),
    tipo_movimiento          varchar(30) NOT NULL CHECK (tipo_movimiento IN
        ('ENTRADA_COMPRA','SALIDA_DISPENSACION','TRASLADO_SALIDA','TRASLADO_ENTRADA',
         'AJUSTE_POSITIVO','AJUSTE_NEGATIVO','DEVOLUCION_PACIENTE','DEVOLUCION_PROVEEDOR',
         'BAJA_VENCIMIENTO','BAJA_DETERIORO')),
    bodega_origen_id         integer REFERENCES bodega(id),
    bodega_destino_id        integer REFERENCES bodega(id),
    lote_id                  integer NOT NULL REFERENCES lote(id),
    servicio_salud_id        integer NOT NULL REFERENCES servicio_salud(id),
    cantidad                 numeric(15,3) NOT NULL CHECK (cantidad > 0),
    valor_unitario           numeric(15,2) NOT NULL DEFAULT 0,
    valor_total              numeric(15,2) GENERATED ALWAYS AS (cantidad * valor_unitario) STORED,
    referencia_tipo          varchar(30),
    referencia_id            integer,
    motivo                   varchar(300),
    fecha_movimiento         timestamp NOT NULL DEFAULT current_timestamp,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    usuario_creacion         integer,
    deleted_at               timestamp,
    CONSTRAINT chk_bodegas_movimiento CHECK (
        (tipo_movimiento IN ('ENTRADA_COMPRA','TRASLADO_ENTRADA','AJUSTE_POSITIVO','DEVOLUCION_PACIENTE')
            AND bodega_destino_id IS NOT NULL) OR
        (tipo_movimiento IN ('SALIDA_DISPENSACION','TRASLADO_SALIDA','AJUSTE_NEGATIVO',
            'DEVOLUCION_PROVEEDOR','BAJA_VENCIMIENTO','BAJA_DETERIORO')
            AND bodega_origen_id IS NOT NULL)
    )
);
```

`referencia_tipo` y `referencia_id` apuntan al documento que originó el movimiento: `'COMPRA'/123`, `'DISPENSACION'/456`, `'TRASLADO'/789`, `'AJUSTE'/12`.

### Tabla nueva 2.8: `solicitud_medicamento`

Pedido desde un servicio (piso, urgencias, quirófano) hacia farmacia central.

```sql
CREATE TABLE solicitud_medicamento (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    sede_id                  integer NOT NULL REFERENCES sede(id),
    numero_solicitud         varchar(30) NOT NULL,
    bodega_origen_id         integer NOT NULL REFERENCES bodega(id),
    bodega_destino_id        integer NOT NULL REFERENCES bodega(id),
    profesional_solicitante_id integer REFERENCES profesional_salud(id),
    estado_solicitud         varchar(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado_solicitud IN
        ('PENDIENTE','EN_PROCESO','DESPACHADA','PARCIAL','RECHAZADA','ANULADA')),
    prioridad                varchar(20) NOT NULL DEFAULT 'NORMAL' CHECK (prioridad IN
        ('NORMAL','URGENTE','VITAL')),
    fecha_solicitud          timestamp NOT NULL DEFAULT current_timestamp,
    fecha_despacho           timestamp,
    motivo                   text,
    observaciones            text,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT uk_solicitud_numero UNIQUE (empresa_id, numero_solicitud)
);
```

### Tabla nueva 2.9: `detalle_solicitud_medicamento`

```sql
CREATE TABLE detalle_solicitud_medicamento (
    id                    serial PRIMARY KEY,
    empresa_id            integer NOT NULL REFERENCES empresa(id),
    solicitud_id          integer NOT NULL REFERENCES solicitud_medicamento(id),
    servicio_salud_id     integer NOT NULL REFERENCES servicio_salud(id),
    cantidad_solicitada   numeric(15,3) NOT NULL CHECK (cantidad_solicitada > 0),
    cantidad_despachada   numeric(15,3) NOT NULL DEFAULT 0,
    estado                varchar(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN
        ('PENDIENTE','DESPACHADO','PARCIAL','RECHAZADO')),
    motivo_rechazo        varchar(300),
    activo                boolean NOT NULL DEFAULT true,
    created_at            timestamp NOT NULL DEFAULT current_timestamp,
    updated_at            timestamp,
    deleted_at            timestamp
);
```

### Tabla nueva 2.10: `dispensacion`

Entrega real al paciente con trazabilidad de qué lote se usó.

```sql
CREATE TABLE dispensacion (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    sede_id                  integer NOT NULL REFERENCES sede(id),
    bodega_id                integer NOT NULL REFERENCES bodega(id),
    numero_dispensacion      varchar(30) NOT NULL,
    prescripcion_id          integer REFERENCES prescripcion(id),
    paciente_id              integer NOT NULL REFERENCES paciente(id),
    profesional_dispensador_id integer NOT NULL REFERENCES profesional_salud(id),
    profesional_receptor_id  integer REFERENCES profesional_salud(id),
    fecha_dispensacion       timestamp NOT NULL DEFAULT current_timestamp,
    estado                   varchar(20) NOT NULL DEFAULT 'COMPLETA' CHECK (estado IN
        ('COMPLETA','PARCIAL','ANULADA')),
    observaciones            text,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT uk_dispensacion_numero UNIQUE (empresa_id, numero_dispensacion)
);
```

### Tabla nueva 2.11: `detalle_dispensacion`

```sql
CREATE TABLE detalle_dispensacion (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    dispensacion_id          integer NOT NULL REFERENCES dispensacion(id),
    detalle_prescripcion_id  integer REFERENCES detalle_prescripcion(id),
    servicio_salud_id        integer NOT NULL REFERENCES servicio_salud(id),
    lote_id                  integer NOT NULL REFERENCES lote(id),
    cantidad                 numeric(15,3) NOT NULL CHECK (cantidad > 0),
    valor_unitario           numeric(15,2),
    observaciones            varchar(300),
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    deleted_at               timestamp
);
```

### Tabla nueva 2.12: `ajuste_inventario`

Ajustes por inventario físico, deterioro, vencimiento, error de captura.

```sql
CREATE TABLE ajuste_inventario (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    sede_id              integer NOT NULL REFERENCES sede(id),
    bodega_id            integer NOT NULL REFERENCES bodega(id),
    numero_ajuste        varchar(30) NOT NULL,
    tipo_ajuste          varchar(30) NOT NULL CHECK (tipo_ajuste IN
        ('FISICO','DETERIORO','VENCIMIENTO','ERROR_CAPTURA','PERDIDA','OTRO')),
    fecha_ajuste         date NOT NULL,
    motivo               text NOT NULL,
    valor_total_ajuste   numeric(18,2) NOT NULL DEFAULT 0,
    aprobado_por_id      integer REFERENCES usuario(id),
    fecha_aprobacion     timestamp,
    estado               varchar(20) NOT NULL DEFAULT 'BORRADOR' CHECK (estado IN
        ('BORRADOR','APROBADO','APLICADO','ANULADO')),
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT uk_ajuste_numero UNIQUE (empresa_id, numero_ajuste)
);
```

### Tabla nueva 2.13: `detalle_ajuste_inventario`

```sql
CREATE TABLE detalle_ajuste_inventario (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    ajuste_id            integer NOT NULL REFERENCES ajuste_inventario(id),
    lote_id              integer NOT NULL REFERENCES lote(id),
    servicio_salud_id    integer NOT NULL REFERENCES servicio_salud(id),
    cantidad_sistema     numeric(15,3) NOT NULL,
    cantidad_real        numeric(15,3) NOT NULL,
    diferencia           numeric(15,3) GENERATED ALWAYS AS (cantidad_real - cantidad_sistema) STORED,
    valor_unitario       numeric(15,2),
    valor_diferencia     numeric(15,2),
    observaciones        varchar(300),
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    deleted_at           timestamp
);
```

### Resumen módulo 2
- **Tablas nuevas**: 13 (`bodega`, `proveedor`, `compra`, `lote`, `detalle_compra`, `stock_lote`, `movimiento_inventario`, `solicitud_medicamento`, `detalle_solicitud_medicamento`, `dispensacion`, `detalle_dispensacion`, `ajuste_inventario`, `detalle_ajuste_inventario`)
- **Tablas reutilizadas**: `servicio_salud`, `prescripcion`, `detalle_prescripcion`, `tercero`, `paciente`, `profesional_salud`

---

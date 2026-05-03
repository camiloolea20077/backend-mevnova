-- ============================================================
-- FASE 2 — Sprint 8 — Módulo: Glosas y Conciliación
-- Script : 12_fase2_glosas.sql
-- Tablas  : glosa, motivo_glosa, concertacion_glosa
-- Alter   : detalle_glosa (agrega motivo_glosa_id)
-- Norma   : Resolución 3047 de 2008 y actualizaciones
-- ============================================================

-- ──────────────────────────────────────────────────────────
-- 0. glosa
--    Encabezado de glosa por factura radicada.
--    Operativa: empresa_id + sede_id (heredados de la factura).
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS glosa (
    id                       serial PRIMARY KEY,
    empresa_id               integer       NOT NULL REFERENCES empresa(id),
    sede_id                  integer       NOT NULL REFERENCES sede(id),
    factura_id               integer       NOT NULL REFERENCES factura(id),
    radicacion_id            integer       REFERENCES radicacion(id),
    numero_oficio_pagador    varchar(50)   NOT NULL,
    fecha_oficio             date          NOT NULL,
    fecha_notificacion       date          NOT NULL,
    valor_total_glosado      numeric(18,2) NOT NULL,
    oficio_url               varchar(500),
    fecha_limite_respuesta   date,
    estado_glosa             varchar(20)   NOT NULL DEFAULT 'ABIERTA' CHECK (estado_glosa IN (
                                 'ABIERTA','EN_RESPUESTA','RESPONDIDA',
                                 'RATIFICADA','CERRADA','ANULADA')),
    observaciones            text,
    activo                   boolean       NOT NULL DEFAULT true,
    created_at               timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT chk_glosa_valor_positivo CHECK (valor_total_glosado > 0)
);

COMMENT ON TABLE glosa IS
    'Encabezado de glosa de pagador sobre una factura radicada. Una factura puede tener varias glosas parciales.';

-- Índice de alto tráfico: bandeja de glosas activas por empresa + estado
CREATE INDEX IF NOT EXISTS idx_glosa_empresa_estado
    ON glosa (empresa_id, estado_glosa)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_glosa_factura
    ON glosa (factura_id)
    WHERE deleted_at IS NULL;

-- ──────────────────────────────────────────────────────────
-- 1. motivo_glosa
--    Catálogo global: sin empresa_id (norma nacional).
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS motivo_glosa (
    id                serial PRIMARY KEY,
    codigo            varchar(10)  NOT NULL,
    nombre            varchar(300) NOT NULL,
    descripcion       text,
    grupo             varchar(50)  NOT NULL CHECK (grupo IN (
                          'FACTURACION','TARIFAS','COBERTURA',
                          'AUTORIZACIONES','PERTINENCIA','DEVOLUCION')),
    aplica_devolucion boolean      NOT NULL DEFAULT false,
    activo            boolean      NOT NULL DEFAULT true,
    created_at        timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at        timestamp,
    deleted_at        timestamp,
    CONSTRAINT uk_motivo_glosa_codigo UNIQUE (codigo)
);

COMMENT ON TABLE motivo_glosa IS
    'Catálogo global de códigos de glosa según Resolución 3047/2008. Sin empresa_id porque la norma es nacional.';

-- ──────────────────────────────────────────────────────────
-- 2. concertacion_glosa
--    Acuerdo de conciliación entre institución y pagador.
--    Transaccional: empresa_id obligatorio.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS concertacion_glosa (
    id                             serial PRIMARY KEY,
    empresa_id                     integer        NOT NULL REFERENCES empresa(id),
    glosa_id                       integer        NOT NULL REFERENCES glosa(id),
    fecha_concertacion             date           NOT NULL,
    valor_glosa_inicial            numeric(18,2)  NOT NULL,
    valor_aceptado_institucion     numeric(18,2)  NOT NULL DEFAULT 0,
    valor_aceptado_pagador         numeric(18,2)  NOT NULL DEFAULT 0,
    -- valor recuperado = lo que la institución no aceptó como glosa válida
    valor_recuperado               numeric(18,2)  GENERATED ALWAYS AS
                                       (valor_glosa_inicial - valor_aceptado_institucion) STORED,
    acta_url                       varchar(500),
    observaciones                  text,
    activo                         boolean        NOT NULL DEFAULT true,
    created_at                     timestamp      NOT NULL DEFAULT current_timestamp,
    updated_at                     timestamp,
    deleted_at                     timestamp,
    usuario_creacion               integer,
    usuario_modificacion           integer,
    CONSTRAINT uk_concertacion_glosa UNIQUE (glosa_id),
    CONSTRAINT chk_concertacion_valores CHECK (
        valor_glosa_inicial   >= 0 AND
        valor_aceptado_institucion >= 0 AND
        valor_aceptado_pagador >= 0 AND
        valor_aceptado_institucion <= valor_glosa_inicial
    )
);

COMMENT ON TABLE concertacion_glosa IS
    'Acuerdo final de conciliación entre institución y pagador. Una glosa tiene como máximo una concertación.';

-- ──────────────────────────────────────────────────────────
-- 3. detalle_glosa
--    Ítem de glosa asociado a un detalle_factura específico.
--    Transaccional: empresa_id obligatorio (E).
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS detalle_glosa (
    id                       serial PRIMARY KEY,
    empresa_id               integer       NOT NULL REFERENCES empresa(id),
    glosa_id                 integer       NOT NULL REFERENCES glosa(id),
    detalle_factura_id       integer       NOT NULL REFERENCES detalle_factura(id),
    motivo_glosa_id          integer       REFERENCES motivo_glosa(id),
    valor_glosado            numeric(18,2) NOT NULL,
    observacion_pagador      text,
    activo                   boolean       NOT NULL DEFAULT true,
    created_at               timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT chk_detalle_glosa_valor CHECK (valor_glosado > 0)
);

COMMENT ON TABLE detalle_glosa IS
    'Ítem de glosa por detalle_factura con motivo oficial Res. 3047. Un mismo detalle_factura puede tener varios motivos.';

CREATE INDEX IF NOT EXISTS idx_detalle_glosa_glosa
    ON detalle_glosa (glosa_id)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_detalle_glosa_detalle_factura
    ON detalle_glosa (detalle_factura_id)
    WHERE deleted_at IS NULL;

-- ──────────────────────────────────────────────────────────
-- 3.1 respuesta_glosa
--     Respuesta institucional a un ítem glosado (1 a 1 con detalle_glosa).
--     Transaccional: empresa_id obligatorio (E).
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS respuesta_glosa (
    id                       serial PRIMARY KEY,
    empresa_id               integer       NOT NULL REFERENCES empresa(id),
    glosa_id                 integer       NOT NULL REFERENCES glosa(id),
    detalle_glosa_id         integer       NOT NULL REFERENCES detalle_glosa(id),
    tipo_respuesta           varchar(20)   NOT NULL CHECK (tipo_respuesta IN (
                                 'ACEPTA_TOTAL','ACEPTA_PARCIAL','NO_ACEPTA')),
    valor_aceptado           numeric(18,2) NOT NULL DEFAULT 0,
    argumentacion            text          NOT NULL,
    soporte_url              varchar(500),
    profesional_respuesta_id integer       REFERENCES profesional_salud(id),
    fecha_respuesta          timestamp     NOT NULL DEFAULT current_timestamp,
    activo                   boolean       NOT NULL DEFAULT true,
    created_at               timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT uk_respuesta_glosa_detalle UNIQUE (detalle_glosa_id),
    CONSTRAINT chk_respuesta_glosa_valor  CHECK (valor_aceptado >= 0)
);

COMMENT ON TABLE respuesta_glosa IS
    'Respuesta institucional a un ítem glosado. Una respuesta por detalle_glosa (UNIQUE).';

CREATE INDEX IF NOT EXISTS idx_respuesta_glosa_glosa
    ON respuesta_glosa (glosa_id)
    WHERE deleted_at IS NULL;

-- ──────────────────────────────────────────────────────────
-- 4. ALTER TABLE detalle_glosa (compatibilidad fase 1 → fase 2)
--    Si la tabla ya existía sin la FK al motivo, agrégala.
--    Documentado: requerido para trazabilidad normativa (Res. 3047).
--    Columna nullable para no romper registros existentes.
-- ──────────────────────────────────────────────────────────
ALTER TABLE detalle_glosa
    ADD COLUMN IF NOT EXISTS motivo_glosa_id integer REFERENCES motivo_glosa(id);

COMMENT ON COLUMN detalle_glosa.motivo_glosa_id IS
    'FK a motivo_glosa. Agragado en fase 2 para trazabilidad normativa Res. 3047. Nullable para compatibilidad con datos de fase 1.';

-- ──────────────────────────────────────────────────────────
-- Índices
-- ──────────────────────────────────────────────────────────

-- motivo_glosa: búsqueda por grupo para listas filtradas
CREATE INDEX IF NOT EXISTS idx_motivo_glosa_grupo
    ON motivo_glosa (grupo)
    WHERE deleted_at IS NULL;

-- concertacion_glosa: alto tráfico → filtrado por empresa
CREATE INDEX IF NOT EXISTS idx_concertacion_glosa_empresa
    ON concertacion_glosa (empresa_id)
    WHERE deleted_at IS NULL;

-- detalle_glosa: tráfico alto → navegación por motivo
CREATE INDEX IF NOT EXISTS idx_detalle_glosa_motivo
    ON detalle_glosa (motivo_glosa_id)
    WHERE motivo_glosa_id IS NOT NULL AND deleted_at IS NULL;

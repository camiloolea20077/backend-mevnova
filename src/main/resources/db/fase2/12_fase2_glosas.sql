-- ============================================================
-- FASE 2 — Sprint 8 — Módulo: Glosas y Conciliación
-- Script : 12_fase2_glosas.sql
-- Tablas  : motivo_glosa, concertacion_glosa
-- Alter   : detalle_glosa (agrega motivo_glosa_id)
-- Norma   : Resolución 3047 de 2008 y actualizaciones
-- ============================================================

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
-- 3. ALTER TABLE detalle_glosa
--    FASE 1 → FASE 2: enlaza ítem de glosa al motivo oficial.
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

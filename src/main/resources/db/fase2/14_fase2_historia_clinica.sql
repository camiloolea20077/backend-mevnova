-- ============================================================
-- FASE 2 — Sprint 8 — Módulo: Historia Clínica Avanzada
-- Script : 14_fase2_historia_clinica.sql
-- Tablas  : tipo_antecedente, antecedente_personal,
--           antecedente_familiar, habito_paciente,
--           revision_sistemas, vacuna_paciente,
--           medicacion_habitual, plan_cuidados_enfermeria,
--           nota_enfermeria, administracion_medicamento,
--           balance_liquidos, detalle_balance_liquidos,
--           escala_clinica, interconsulta, epicrisis,
--           adjunto_clinico, consentimiento_informado
-- Alto tráfico: nota_enfermeria, administracion_medicamento
-- Dependencias farmacia: administracion_medicamento referencia
--   dispensacion y lote (definidos en 13_fase2_farmacia.sql)
-- ============================================================

-- ──────────────────────────────────────────────────────────
-- 1. tipo_antecedente
--    Catálogo global: sin empresa_id.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tipo_antecedente (
    id          serial PRIMARY KEY,
    codigo      varchar(20)  NOT NULL,
    nombre      varchar(150) NOT NULL,
    descripcion text,
    activo      boolean      NOT NULL DEFAULT true,
    created_at  timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at  timestamp,
    deleted_at  timestamp,
    CONSTRAINT uk_tipo_antecedente_codigo UNIQUE (codigo)
);

COMMENT ON TABLE tipo_antecedente IS
    'Catálogo global de tipos de antecedente clínico. Sin empresa_id porque es estándar clínico compartido.';

-- ──────────────────────────────────────────────────────────
-- 2. antecedente_personal
--    Antecedentes médicos del paciente.
--    Transaccional: empresa_id obligatorio.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS antecedente_personal (
    id                       serial PRIMARY KEY,
    empresa_id               integer     NOT NULL REFERENCES empresa(id),
    paciente_id              integer     NOT NULL REFERENCES paciente(id),
    tipo_antecedente_id      integer     NOT NULL REFERENCES tipo_antecedente(id),
    catalogo_diagnostico_id  integer     REFERENCES catalogo_diagnostico(id),
    descripcion              text        NOT NULL,
    fecha_inicio             date,
    fecha_fin                date,
    es_activo                boolean     NOT NULL DEFAULT true,
    severidad                varchar(20) CHECK (severidad IN ('LEVE','MODERADA','GRAVE','CRITICA')),
    observaciones            text,
    profesional_registro_id  integer     REFERENCES profesional_salud(id),
    activo                   boolean     NOT NULL DEFAULT true,
    created_at               timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT chk_antecedente_fechas CHECK (
        fecha_inicio IS NULL OR fecha_fin IS NULL OR fecha_inicio <= fecha_fin
    )
);

COMMENT ON TABLE antecedente_personal IS
    'Antecedentes personales del paciente clasificados por tipo (patológico, quirúrgico, alérgico, etc.).';

-- ──────────────────────────────────────────────────────────
-- 3. antecedente_familiar
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS antecedente_familiar (
    id                       serial PRIMARY KEY,
    empresa_id               integer   NOT NULL REFERENCES empresa(id),
    paciente_id              integer   NOT NULL REFERENCES paciente(id),
    parentesco               varchar(50) NOT NULL,
    catalogo_diagnostico_id  integer   REFERENCES catalogo_diagnostico(id),
    descripcion              text      NOT NULL,
    edad_aparicion           integer,
    es_fallecido             boolean   NOT NULL DEFAULT false,
    causa_fallecimiento      varchar(300),
    observaciones            text,
    activo                   boolean   NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT chk_antecedente_familiar_edad CHECK (
        edad_aparicion IS NULL OR edad_aparicion >= 0
    )
);

COMMENT ON TABLE antecedente_familiar IS
    'Antecedentes de salud en familiares directos del paciente. Útil para riesgo hereditario.';

-- ──────────────────────────────────────────────────────────
-- 4. habito_paciente
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS habito_paciente (
    id                   serial PRIMARY KEY,
    empresa_id           integer     NOT NULL REFERENCES empresa(id),
    paciente_id          integer     NOT NULL REFERENCES paciente(id),
    tipo_habito          varchar(30) NOT NULL CHECK (tipo_habito IN (
                             'ALCOHOL','TABACO','SUSTANCIAS_PSICOACTIVAS',
                             'EJERCICIO','ALIMENTACION','SUENO','SEXUAL','OTRO')),
    descripcion          text        NOT NULL,
    frecuencia           varchar(100),
    cantidad             varchar(100),
    tiempo_consumo       varchar(100),
    fecha_inicio         date,
    fecha_fin            date,
    estado               varchar(20) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN (
                             'ACTIVO','EX_CONSUMIDOR','NUNCA','OCASIONAL')),
    observaciones        text,
    activo               boolean     NOT NULL DEFAULT true,
    created_at           timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer
);

COMMENT ON TABLE habito_paciente IS
    'Hábitos y estilos de vida del paciente (alcohol, tabaco, ejercicio, alimentación, etc.).';

-- ──────────────────────────────────────────────────────────
-- 5. revision_sistemas
--    Revisión por sistemas por atención.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS revision_sistemas (
    id               serial PRIMARY KEY,
    empresa_id       integer     NOT NULL REFERENCES empresa(id),
    atencion_id      integer     NOT NULL REFERENCES atencion(id),
    sistema          varchar(30) NOT NULL CHECK (sistema IN (
                         'CARDIOVASCULAR','RESPIRATORIO','GASTROINTESTINAL',
                         'GENITOURINARIO','NEUROLOGICO','MUSCULOESQUELETICO',
                         'PIEL_FANERAS','HEMATOLOGICO','ENDOCRINO',
                         'OFTALMOLOGICO','OTORRINO','PSIQUIATRICO','GENERAL')),
    sin_alteracion   boolean     NOT NULL DEFAULT true,
    hallazgos        text,
    activo           boolean     NOT NULL DEFAULT true,
    created_at       timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at       timestamp,
    deleted_at       timestamp,
    usuario_creacion integer,
    CONSTRAINT uk_revision_sistemas UNIQUE (atencion_id, sistema)
);

COMMENT ON TABLE revision_sistemas IS
    'Revisión por sistemas asociada a una atención. Una atención tiene un registro por sistema evaluado.';

-- ──────────────────────────────────────────────────────────
-- 6. vacuna_paciente
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vacuna_paciente (
    id                    serial PRIMARY KEY,
    empresa_id            integer     NOT NULL REFERENCES empresa(id),
    paciente_id           integer     NOT NULL REFERENCES paciente(id),
    nombre_vacuna         varchar(150) NOT NULL,
    codigo_vacuna         varchar(30),
    dosis                 integer     NOT NULL,
    total_dosis_esquema   integer,
    fecha_aplicacion      date        NOT NULL,
    fecha_proxima_dosis   date,
    laboratorio           varchar(100),
    numero_lote           varchar(50),
    via_administracion_id integer     REFERENCES via_administracion(id),
    profesional_aplica_id integer     REFERENCES profesional_salud(id),
    institucion_aplica    varchar(200),
    observaciones         text,
    activo                boolean     NOT NULL DEFAULT true,
    created_at            timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at            timestamp,
    deleted_at            timestamp,
    usuario_creacion      integer,
    usuario_modificacion  integer,
    CONSTRAINT chk_vacuna_dosis CHECK (
        dosis >= 1 AND
        (total_dosis_esquema IS NULL OR dosis <= total_dosis_esquema)
    )
);

COMMENT ON TABLE vacuna_paciente IS
    'Registro del esquema de vacunación del paciente por dosis aplicada.';

-- ──────────────────────────────────────────────────────────
-- 7. medicacion_habitual
--    Medicamentos crónicos del paciente.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS medicacion_habitual (
    id                       serial PRIMARY KEY,
    empresa_id               integer      NOT NULL REFERENCES empresa(id),
    paciente_id              integer      NOT NULL REFERENCES paciente(id),
    servicio_salud_id        integer      REFERENCES servicio_salud(id),
    nombre_medicamento       varchar(200) NOT NULL,
    dosis                    varchar(50),
    via_administracion_id    integer      REFERENCES via_administracion(id),
    frecuencia_dosis_id      integer      REFERENCES frecuencia_dosis(id),
    fecha_inicio             date,
    fecha_fin                date,
    indicacion               varchar(300),
    profesional_prescriptor  varchar(200),
    es_activo                boolean      NOT NULL DEFAULT true,
    observaciones            text,
    activo                   boolean      NOT NULL DEFAULT true,
    created_at               timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer
);

COMMENT ON TABLE medicacion_habitual IS
    'Medicamentos que el paciente consume de forma crónica antes de la atención actual.';

-- ──────────────────────────────────────────────────────────
-- 8. plan_cuidados_enfermeria
--    Plan de cuidados al ingreso o durante hospitalización.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS plan_cuidados_enfermeria (
    id                     serial PRIMARY KEY,
    empresa_id             integer     NOT NULL REFERENCES empresa(id),
    sede_id                integer     NOT NULL REFERENCES sede(id),
    atencion_id            integer     NOT NULL REFERENCES atencion(id),
    paciente_id            integer     NOT NULL REFERENCES paciente(id),
    profesional_id         integer     NOT NULL REFERENCES profesional_salud(id),
    fecha_plan             date        NOT NULL DEFAULT current_date,
    diagnostico_enfermeria text        NOT NULL,
    objetivos              text        NOT NULL,
    intervenciones         text        NOT NULL,
    evaluacion             text,
    estado                 varchar(20) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN (
                               'ACTIVO','CUMPLIDO','MODIFICADO','SUSPENDIDO')),
    activo                 boolean     NOT NULL DEFAULT true,
    created_at             timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at             timestamp,
    deleted_at             timestamp,
    usuario_creacion       integer,
    usuario_modificacion   integer
);

COMMENT ON TABLE plan_cuidados_enfermeria IS
    'Plan de cuidados de enfermería NANDA/NIC/NOC por atención. Permite registrar diagnóstico, objetivos e intervenciones.';

-- ──────────────────────────────────────────────────────────
-- 9. nota_enfermeria  [ALTO TRÁFICO]
--    Notas clínicas del turno de enfermería.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS nota_enfermeria (
    id                      serial PRIMARY KEY,
    empresa_id              integer      NOT NULL REFERENCES empresa(id),
    sede_id                 integer      NOT NULL REFERENCES sede(id),
    atencion_id             integer      NOT NULL REFERENCES atencion(id),
    paciente_id             integer      NOT NULL REFERENCES paciente(id),
    profesional_id          integer      NOT NULL REFERENCES profesional_salud(id),
    tipo_nota               varchar(30)  NOT NULL CHECK (tipo_nota IN (
                                'INGRESO','EVOLUCION','NOVEDAD','ENTREGA_TURNO',
                                'POST_PROCEDIMIENTO','EDUCACION',
                                'PRE_QUIRURGICA','POST_QUIRURGICA')),
    turno                   varchar(10)  CHECK (turno IN ('MANANA','TARDE','NOCHE')),
    fecha_nota              timestamp    NOT NULL DEFAULT current_timestamp,
    contenido               text         NOT NULL,
    -- Signos vitales opcionales embebidos en la nota
    tension_sistolica       integer,
    tension_diastolica      integer,
    frecuencia_cardiaca     integer,
    frecuencia_respiratoria integer,
    temperatura             numeric(4,1),
    saturacion_oxigeno      integer,
    glucometria             numeric(5,1),
    dolor_eva               integer      CHECK (dolor_eva BETWEEN 0 AND 10),
    firmada                 boolean      NOT NULL DEFAULT false,
    fecha_firma             timestamp,
    activo                  boolean      NOT NULL DEFAULT true,
    created_at              timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at              timestamp,
    deleted_at              timestamp,
    usuario_creacion        integer,
    usuario_modificacion    integer,
    CONSTRAINT chk_nota_enfermeria_signos CHECK (
        tension_sistolica   IS NULL OR tension_sistolica   BETWEEN 0 AND 300 AND
        tension_diastolica  IS NULL OR tension_diastolica  BETWEEN 0 AND 200 AND
        frecuencia_cardiaca IS NULL OR frecuencia_cardiaca BETWEEN 0 AND 300 AND
        saturacion_oxigeno  IS NULL OR saturacion_oxigeno  BETWEEN 0 AND 100
    )
);

COMMENT ON TABLE nota_enfermeria IS
    'Nota clínica del turno de enfermería. Alto tráfico en hospitalización: múltiples notas por paciente por turno.';

-- ──────────────────────────────────────────────────────────
-- 10. administracion_medicamento  [ALTO TRÁFICO — MAR]
--     Medication Administration Record: dosis programadas y administradas.
--     Depende de dispensacion y lote (13_fase2_farmacia.sql).
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS administracion_medicamento (
    id                       serial PRIMARY KEY,
    empresa_id               integer       NOT NULL REFERENCES empresa(id),
    sede_id                  integer       NOT NULL REFERENCES sede(id),
    atencion_id              integer       NOT NULL REFERENCES atencion(id),
    paciente_id              integer       NOT NULL REFERENCES paciente(id),
    detalle_prescripcion_id  integer       NOT NULL REFERENCES detalle_prescripcion(id),
    dispensacion_id          integer       REFERENCES dispensacion(id),
    lote_id                  integer       REFERENCES lote(id),
    profesional_id           integer       NOT NULL REFERENCES profesional_salud(id),
    fecha_programada         timestamp     NOT NULL,
    fecha_administracion     timestamp,
    dosis_administrada       numeric(10,2),
    via_administracion_id    integer       REFERENCES via_administracion(id),
    estado                   varchar(20)   NOT NULL DEFAULT 'PROGRAMADA' CHECK (estado IN (
                                 'PROGRAMADA','ADMINISTRADA','OMITIDA',
                                 'RECHAZADA','SUSPENDIDA')),
    motivo_omision           varchar(300),
    reaccion_adversa         text,
    observaciones            text,
    activo                   boolean       NOT NULL DEFAULT true,
    created_at               timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT chk_mar_fecha_admin CHECK (
        estado = 'PROGRAMADA' OR fecha_administracion IS NOT NULL OR
        estado IN ('OMITIDA','RECHAZADA','SUSPENDIDA')
    )
);

COMMENT ON TABLE administracion_medicamento IS
    'MAR (Medication Administration Record). Registra cada dosis programada y su estado de administración real.';

-- ──────────────────────────────────────────────────────────
-- 11. balance_liquidos
--     Encabezado de balance hídrico por turno.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS balance_liquidos (
    id                   serial PRIMARY KEY,
    empresa_id           integer       NOT NULL REFERENCES empresa(id),
    sede_id              integer       NOT NULL REFERENCES sede(id),
    atencion_id          integer       NOT NULL REFERENCES atencion(id),
    paciente_id          integer       NOT NULL REFERENCES paciente(id),
    profesional_id       integer       NOT NULL REFERENCES profesional_salud(id),
    fecha_balance        date          NOT NULL DEFAULT current_date,
    turno                varchar(10)   CHECK (turno IN ('MANANA','TARDE','NOCHE','DIA_24H')),
    total_ingresos       numeric(10,2) NOT NULL DEFAULT 0,
    total_egresos        numeric(10,2) NOT NULL DEFAULT 0,
    balance              numeric(10,2) GENERATED ALWAYS AS (total_ingresos - total_egresos) STORED,
    observaciones        text,
    activo               boolean       NOT NULL DEFAULT true,
    created_at           timestamp     NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer,
    CONSTRAINT chk_balance_liquidos_valores CHECK (
        total_ingresos >= 0 AND total_egresos >= 0
    )
);

COMMENT ON TABLE balance_liquidos IS
    'Encabezado del balance hídrico por turno. El detalle de cada registro va en detalle_balance_liquidos.';

-- ──────────────────────────────────────────────────────────
-- 12. detalle_balance_liquidos
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS detalle_balance_liquidos (
    id            serial PRIMARY KEY,
    empresa_id    integer       NOT NULL REFERENCES empresa(id),
    balance_id    integer       NOT NULL REFERENCES balance_liquidos(id),
    tipo          varchar(20)   NOT NULL CHECK (tipo IN ('INGRESO','EGRESO')),
    via           varchar(30)   NOT NULL CHECK (via IN (
                      'ORAL','IV','SNG','SVD','DRENAJE','VOMITO',
                      'DEPOSICION','SUDORACION','INSENSIBLES','OTRO')),
    descripcion   varchar(200),
    cantidad_ml   numeric(10,2) NOT NULL CHECK (cantidad_ml >= 0),
    hora_registro time          NOT NULL,
    activo        boolean       NOT NULL DEFAULT true,
    created_at    timestamp     NOT NULL DEFAULT current_timestamp,
    deleted_at    timestamp
);

COMMENT ON TABLE detalle_balance_liquidos IS
    'Registro individual de ingreso o egreso hídrico. Sin updated_at: los registros de balance son inmutables.';

-- ──────────────────────────────────────────────────────────
-- 13. escala_clinica
--     Aplicación de escalas (Glasgow, EVA, Norton, Morse, etc.)
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS escala_clinica (
    id               serial PRIMARY KEY,
    empresa_id       integer     NOT NULL REFERENCES empresa(id),
    sede_id          integer     NOT NULL REFERENCES sede(id),
    atencion_id      integer     NOT NULL REFERENCES atencion(id),
    paciente_id      integer     NOT NULL REFERENCES paciente(id),
    profesional_id   integer     NOT NULL REFERENCES profesional_salud(id),
    tipo_escala      varchar(30) NOT NULL CHECK (tipo_escala IN (
                         'GLASGOW','EVA','NORTON','BRADEN','MORSE','DOWNTON',
                         'BARTHEL','LAWTON','KATZ','MINI_MENTAL','APGAR',
                         'SILVERMAN','GLASGOW_PEDIATRICO','OTRA')),
    fecha_aplicacion timestamp   NOT NULL DEFAULT current_timestamp,
    puntaje_total    integer     NOT NULL,
    interpretacion   varchar(200),
    riesgo           varchar(20) CHECK (riesgo IN ('BAJO','MEDIO','ALTO','MUY_ALTO')),
    detalle_escala   jsonb,
    observaciones    text,
    activo           boolean     NOT NULL DEFAULT true,
    created_at       timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at       timestamp,
    deleted_at       timestamp,
    usuario_creacion       integer,
    usuario_modificacion   integer
);

COMMENT ON TABLE escala_clinica IS
    'Aplicación de escalas clínicas estandarizadas. detalle_escala (jsonb) contiene los ítems individuales de cada tipo.';

-- ──────────────────────────────────────────────────────────
-- 14. interconsulta
--     Solicitud y respuesta entre especialidades.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS interconsulta (
    id                       serial PRIMARY KEY,
    empresa_id               integer     NOT NULL REFERENCES empresa(id),
    sede_id                  integer     NOT NULL REFERENCES sede(id),
    atencion_origen_id       integer     NOT NULL REFERENCES atencion(id),
    atencion_respuesta_id    integer     REFERENCES atencion(id),
    numero_interconsulta     varchar(30) NOT NULL,
    profesional_solicita_id  integer     NOT NULL REFERENCES profesional_salud(id),
    profesional_responde_id  integer     REFERENCES profesional_salud(id),
    especialidad_destino_id  integer     NOT NULL REFERENCES especialidad(id),
    motivo                   text        NOT NULL,
    impresion_diagnostica    text,
    pregunta_clinica         text,
    estado                   varchar(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN (
                                 'PENDIENTE','EN_PROCESO','RESPONDIDA','ANULADA')),
    prioridad                varchar(20) DEFAULT 'NORMAL' CHECK (prioridad IN (
                                 'NORMAL','URGENTE','VITAL')),
    fecha_solicitud          timestamp   NOT NULL DEFAULT current_timestamp,
    fecha_respuesta          timestamp,
    respuesta                text,
    recomendaciones          text,
    activo                   boolean     NOT NULL DEFAULT true,
    created_at               timestamp   NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT uk_interconsulta UNIQUE (empresa_id, numero_interconsulta),
    CONSTRAINT chk_interconsulta_atenciones CHECK (
        atencion_respuesta_id IS NULL OR atencion_respuesta_id <> atencion_origen_id
    )
);

COMMENT ON TABLE interconsulta IS
    'Solicitud de interconsulta a otra especialidad. atencion_respuesta_id se llena cuando el especialista responde.';

-- ──────────────────────────────────────────────────────────
-- 15. epicrisis
--     Resumen estructurado al egreso hospitalario.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS epicrisis (
    id                        serial PRIMARY KEY,
    empresa_id                integer      NOT NULL REFERENCES empresa(id),
    sede_id                   integer      NOT NULL REFERENCES sede(id),
    admision_id               integer      NOT NULL REFERENCES admision(id),
    paciente_id               integer      NOT NULL REFERENCES paciente(id),
    profesional_id            integer      NOT NULL REFERENCES profesional_salud(id),
    fecha_egreso              timestamp    NOT NULL,
    motivo_ingreso            text         NOT NULL,
    diagnostico_ingreso       text         NOT NULL,
    diagnostico_egreso        text         NOT NULL,
    procedimientos_realizados text,
    evolucion_resumen         text         NOT NULL,
    complicaciones            text,
    plan_seguimiento          text         NOT NULL,
    medicamentos_egreso       text,
    recomendaciones           text         NOT NULL,
    indicaciones_dieta        text,
    indicaciones_actividad    text,
    fecha_proximo_control     date,
    firmada                   boolean      NOT NULL DEFAULT false,
    fecha_firma               timestamp,
    pdf_url                   varchar(500),
    activo                    boolean      NOT NULL DEFAULT true,
    created_at                timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at                timestamp,
    deleted_at                timestamp,
    usuario_creacion          integer,
    usuario_modificacion      integer,
    CONSTRAINT uk_epicrisis_admision UNIQUE (admision_id)
);

COMMENT ON TABLE epicrisis IS
    'Resumen estructurado al egreso hospitalario. Una admision tiene como máximo una epicrisis.';

-- ──────────────────────────────────────────────────────────
-- 16. adjunto_clinico
--     Documentos adjuntos a la HC del paciente.
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS adjunto_clinico (
    id                   serial PRIMARY KEY,
    empresa_id           integer      NOT NULL REFERENCES empresa(id),
    sede_id              integer      NOT NULL REFERENCES sede(id),
    paciente_id          integer      NOT NULL REFERENCES paciente(id),
    atencion_id          integer      REFERENCES atencion(id),
    tipo_documento       varchar(30)  NOT NULL CHECK (tipo_documento IN (
                             'RESULTADO_LABORATORIO','IMAGEN_DIAGNOSTICA',
                             'REPORTE_PATOLOGIA','CONSENTIMIENTO',
                             'EXAMEN_EXTERNO','FOTO_CLINICA','ECG','OTRO')),
    nombre_archivo       varchar(300) NOT NULL,
    descripcion          varchar(500),
    url_archivo          varchar(500) NOT NULL,
    mime_type            varchar(100),
    tamano_bytes         bigint,
    profesional_carga_id integer      REFERENCES profesional_salud(id),
    fecha_documento      date,
    es_confidencial      boolean      NOT NULL DEFAULT false,
    activo               boolean      NOT NULL DEFAULT true,
    created_at           timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer
);

COMMENT ON TABLE adjunto_clinico IS
    'Documentos adjuntos a la historia clínica: resultados externos, imágenes, consentimientos escaneados, fotos clínicas.';

-- ──────────────────────────────────────────────────────────
-- 17. consentimiento_informado
-- ──────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS consentimiento_informado (
    id                      serial PRIMARY KEY,
    empresa_id              integer      NOT NULL REFERENCES empresa(id),
    sede_id                 integer      NOT NULL REFERENCES sede(id),
    paciente_id             integer      NOT NULL REFERENCES paciente(id),
    atencion_id             integer      REFERENCES atencion(id),
    tipo_consentimiento     varchar(50)  NOT NULL,
    procedimiento           varchar(300),
    profesional_id          integer      NOT NULL REFERENCES profesional_salud(id),
    representante_legal_id  integer      REFERENCES tercero(id),
    fecha_firma             timestamp    NOT NULL DEFAULT current_timestamp,
    contenido               text         NOT NULL,
    pdf_url                 varchar(500),
    firma_paciente_url      varchar(500),
    activo                  boolean      NOT NULL DEFAULT true,
    created_at              timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at              timestamp,
    deleted_at              timestamp,
    usuario_creacion        integer,
    usuario_modificacion    integer
);

COMMENT ON TABLE consentimiento_informado IS
    'Registro del consentimiento informado firmado por el paciente o representante legal antes de un procedimiento.';

-- ──────────────────────────────────────────────────────────
-- Índices
-- ──────────────────────────────────────────────────────────

-- antecedente_personal: búsqueda por paciente (historia clínica)
CREATE INDEX IF NOT EXISTS idx_antecedente_personal_paciente
    ON antecedente_personal (empresa_id, paciente_id)
    WHERE deleted_at IS NULL;

-- antecedente_familiar
CREATE INDEX IF NOT EXISTS idx_antecedente_familiar_paciente
    ON antecedente_familiar (empresa_id, paciente_id)
    WHERE deleted_at IS NULL;

-- habito_paciente
CREATE INDEX IF NOT EXISTS idx_habito_paciente_paciente
    ON habito_paciente (empresa_id, paciente_id)
    WHERE deleted_at IS NULL;

-- revision_sistemas: por atención
CREATE INDEX IF NOT EXISTS idx_revision_sistemas_atencion
    ON revision_sistemas (empresa_id, atencion_id)
    WHERE deleted_at IS NULL;

-- vacuna_paciente
CREATE INDEX IF NOT EXISTS idx_vacuna_paciente_paciente
    ON vacuna_paciente (empresa_id, paciente_id)
    WHERE deleted_at IS NULL;

-- nota_enfermeria [ALTO TRÁFICO]: filtrado por atención (hospitalización activa)
CREATE INDEX IF NOT EXISTS idx_nota_enfermeria_atencion
    ON nota_enfermeria (empresa_id, atencion_id, fecha_nota DESC)
    WHERE deleted_at IS NULL;

-- nota_enfermeria: filtrado por paciente
CREATE INDEX IF NOT EXISTS idx_nota_enfermeria_paciente
    ON nota_enfermeria (empresa_id, paciente_id, fecha_nota DESC)
    WHERE deleted_at IS NULL;

-- administracion_medicamento [ALTO TRÁFICO — MAR]: pendientes por atención
CREATE INDEX IF NOT EXISTS idx_adm_medicamento_atencion_estado
    ON administracion_medicamento (empresa_id, atencion_id, estado)
    WHERE deleted_at IS NULL;

-- administracion_medicamento: trazabilidad por prescripcion
CREATE INDEX IF NOT EXISTS idx_adm_medicamento_prescripcion
    ON administracion_medicamento (empresa_id, detalle_prescripcion_id)
    WHERE deleted_at IS NULL;

-- administracion_medicamento: programación por fecha (vista de turno)
CREATE INDEX IF NOT EXISTS idx_adm_medicamento_fecha
    ON administracion_medicamento (empresa_id, sede_id, fecha_programada)
    WHERE deleted_at IS NULL AND estado = 'PROGRAMADA';

-- balance_liquidos: por atención y turno
CREATE INDEX IF NOT EXISTS idx_balance_liquidos_atencion
    ON balance_liquidos (empresa_id, atencion_id, fecha_balance DESC)
    WHERE deleted_at IS NULL;

-- escala_clinica: búsqueda por tipo y atención
CREATE INDEX IF NOT EXISTS idx_escala_clinica_atencion_tipo
    ON escala_clinica (empresa_id, atencion_id, tipo_escala)
    WHERE deleted_at IS NULL;

-- interconsulta: bandeja de pendientes
CREATE INDEX IF NOT EXISTS idx_interconsulta_estado
    ON interconsulta (empresa_id, estado, especialidad_destino_id)
    WHERE deleted_at IS NULL;

-- adjunto_clinico: documentos del paciente
CREATE INDEX IF NOT EXISTS idx_adjunto_clinico_paciente
    ON adjunto_clinico (empresa_id, paciente_id)
    WHERE deleted_at IS NULL;

-- plan_cuidados_enfermeria: por atención activa
CREATE INDEX IF NOT EXISTS idx_plan_cuidados_atencion
    ON plan_cuidados_enfermeria (empresa_id, atencion_id)
    WHERE deleted_at IS NULL;

# Modelo Historia Clinica

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

## Módulo 3: Historia clínica avanzada

### Contexto del negocio

La historia clínica completa requiere mucho más que el SOAP básico embebido en `atencion`. Necesitas estructuras dedicadas para antecedentes, hábitos, vacunas, notas de enfermería, MAR (administración de medicamentos), balances, escalas e interconsultas. Toda esta información se asocia al **paciente** (no a una atención específica) o a la **atención** según el caso.

### Tabla nueva 3.1: `tipo_antecedente` (catálogo global)

```sql
CREATE TABLE tipo_antecedente (
    id           serial PRIMARY KEY,
    codigo       varchar(20) NOT NULL UNIQUE,
    nombre       varchar(150) NOT NULL,
    descripcion  text,
    activo       boolean NOT NULL DEFAULT true,
    created_at   timestamp NOT NULL DEFAULT current_timestamp,
    updated_at   timestamp,
    deleted_at   timestamp
);
```

Valores semilla: `PATOLOGICO`, `QUIRURGICO`, `TRAUMATICO`, `ALERGICO`, `TOXICO`, `FARMACOLOGICO`, `GINECO_OBSTETRICO`, `PSIQUIATRICO`, `HOSPITALARIO`, `TRANSFUSIONAL`.

### Tabla nueva 3.2: `antecedente_personal`

Antecedentes del paciente clasificados por tipo.

```sql
CREATE TABLE antecedente_personal (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    paciente_id              integer NOT NULL REFERENCES paciente(id),
    tipo_antecedente_id      integer NOT NULL REFERENCES tipo_antecedente(id),
    catalogo_diagnostico_id  integer REFERENCES catalogo_diagnostico(id),
    descripcion              text NOT NULL,
    fecha_inicio             date,
    fecha_fin                date,
    es_activo                boolean NOT NULL DEFAULT true,
    severidad                varchar(20) CHECK (severidad IN ('LEVE','MODERADA','GRAVE','CRITICA')),
    observaciones            text,
    profesional_registro_id  integer REFERENCES profesional_salud(id),
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer
);
```

### Tabla nueva 3.3: `antecedente_familiar`

```sql
CREATE TABLE antecedente_familiar (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    paciente_id              integer NOT NULL REFERENCES paciente(id),
    parentesco               varchar(50) NOT NULL,
    catalogo_diagnostico_id  integer REFERENCES catalogo_diagnostico(id),
    descripcion              text NOT NULL,
    edad_aparicion           integer,
    es_fallecido             boolean NOT NULL DEFAULT false,
    causa_fallecimiento      varchar(300),
    observaciones            text,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer
);
```

### Tabla nueva 3.4: `habito_paciente`

```sql
CREATE TABLE habito_paciente (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    paciente_id          integer NOT NULL REFERENCES paciente(id),
    tipo_habito          varchar(30) NOT NULL CHECK (tipo_habito IN
        ('ALCOHOL','TABACO','SUSTANCIAS_PSICOACTIVAS','EJERCICIO','ALIMENTACION',
         'SUENO','SEXUAL','OTRO')),
    descripcion          text NOT NULL,
    frecuencia           varchar(100),
    cantidad             varchar(100),
    tiempo_consumo       varchar(100),
    fecha_inicio         date,
    fecha_fin            date,
    estado               varchar(20) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN
        ('ACTIVO','EX_CONSUMIDOR','NUNCA','OCASIONAL')),
    observaciones        text,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer
);
```

### Tabla nueva 3.5: `revision_sistemas`

Revisión por sistemas asociada a una atención.

```sql
CREATE TABLE revision_sistemas (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    atencion_id          integer NOT NULL REFERENCES atencion(id),
    sistema              varchar(30) NOT NULL CHECK (sistema IN
        ('CARDIOVASCULAR','RESPIRATORIO','GASTROINTESTINAL','GENITOURINARIO',
         'NEUROLOGICO','MUSCULOESQUELETICO','PIEL_FANERAS','HEMATOLOGICO',
         'ENDOCRINO','OFTALMOLOGICO','OTORRINO','PSIQUIATRICO','GENERAL')),
    sin_alteracion       boolean NOT NULL DEFAULT true,
    hallazgos            text,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer
);
```

### Tabla nueva 3.6: `vacuna_paciente`

Esquema de vacunación del paciente.

```sql
CREATE TABLE vacuna_paciente (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    paciente_id          integer NOT NULL REFERENCES paciente(id),
    nombre_vacuna        varchar(150) NOT NULL,
    codigo_vacuna        varchar(30),
    dosis                integer NOT NULL,
    total_dosis_esquema  integer,
    fecha_aplicacion     date NOT NULL,
    fecha_proxima_dosis  date,
    laboratorio          varchar(100),
    numero_lote          varchar(50),
    via_administracion_id integer REFERENCES via_administracion(id),
    profesional_aplica_id integer REFERENCES profesional_salud(id),
    institucion_aplica   varchar(200),
    observaciones        text,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer
);
```

### Tabla nueva 3.7: `medicacion_habitual`

Medicamentos que el paciente toma de forma crónica.

```sql
CREATE TABLE medicacion_habitual (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    paciente_id              integer NOT NULL REFERENCES paciente(id),
    servicio_salud_id        integer REFERENCES servicio_salud(id),
    nombre_medicamento       varchar(200) NOT NULL,
    dosis                    varchar(50),
    via_administracion_id    integer REFERENCES via_administracion(id),
    frecuencia_dosis_id      integer REFERENCES frecuencia_dosis(id),
    fecha_inicio             date,
    fecha_fin                date,
    indicacion               varchar(300),
    profesional_prescriptor  varchar(200),
    es_activo                boolean NOT NULL DEFAULT true,
    observaciones            text,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer
);
```

### Tabla nueva 3.8: `plan_cuidados_enfermeria`

Plan de cuidados al ingreso o durante la hospitalización.

```sql
CREATE TABLE plan_cuidados_enfermeria (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    sede_id                  integer NOT NULL REFERENCES sede(id),
    atencion_id              integer NOT NULL REFERENCES atencion(id),
    paciente_id              integer NOT NULL REFERENCES paciente(id),
    profesional_id           integer NOT NULL REFERENCES profesional_salud(id),
    fecha_plan               date NOT NULL DEFAULT current_date,
    diagnostico_enfermeria   text NOT NULL,
    objetivos                text NOT NULL,
    intervenciones           text NOT NULL,
    evaluacion               text,
    estado                   varchar(20) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN
        ('ACTIVO','CUMPLIDO','MODIFICADO','SUSPENDIDO')),
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer
);
```

### Tabla nueva 3.9: `nota_enfermeria`

Notas de enfermería: ingreso, evolución, novedad, entrega de turno, post-procedimiento.

```sql
CREATE TABLE nota_enfermeria (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    sede_id                  integer NOT NULL REFERENCES sede(id),
    atencion_id              integer NOT NULL REFERENCES atencion(id),
    paciente_id              integer NOT NULL REFERENCES paciente(id),
    profesional_id           integer NOT NULL REFERENCES profesional_salud(id),
    tipo_nota                varchar(30) NOT NULL CHECK (tipo_nota IN
        ('INGRESO','EVOLUCION','NOVEDAD','ENTREGA_TURNO','POST_PROCEDIMIENTO',
         'EDUCACION','PRE_QUIRURGICA','POST_QUIRURGICA')),
    turno                    varchar(10) CHECK (turno IN ('MANANA','TARDE','NOCHE')),
    fecha_nota               timestamp NOT NULL DEFAULT current_timestamp,
    contenido                text NOT NULL,
    -- Signos vitales asociados a la nota (opcional)
    tension_sistolica        integer,
    tension_diastolica       integer,
    frecuencia_cardiaca      integer,
    frecuencia_respiratoria  integer,
    temperatura              numeric(4,1),
    saturacion_oxigeno       integer,
    glucometria              numeric(5,1),
    dolor_eva                integer CHECK (dolor_eva BETWEEN 0 AND 10),
    firmada                  boolean NOT NULL DEFAULT false,
    fecha_firma              timestamp,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer
);
```

### Tabla nueva 3.10: `administracion_medicamento` (MAR)

Registro de administración real de cada dosis (MAR — Medication Administration Record).

```sql
CREATE TABLE administracion_medicamento (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    sede_id                  integer NOT NULL REFERENCES sede(id),
    atencion_id              integer NOT NULL REFERENCES atencion(id),
    paciente_id              integer NOT NULL REFERENCES paciente(id),
    detalle_prescripcion_id  integer NOT NULL REFERENCES detalle_prescripcion(id),
    dispensacion_id          integer REFERENCES dispensacion(id),
    lote_id                  integer REFERENCES lote(id),
    profesional_id           integer NOT NULL REFERENCES profesional_salud(id),
    fecha_programada         timestamp NOT NULL,
    fecha_administracion     timestamp,
    dosis_administrada       numeric(10,2),
    via_administracion_id    integer REFERENCES via_administracion(id),
    estado                   varchar(20) NOT NULL DEFAULT 'PROGRAMADA' CHECK (estado IN
        ('PROGRAMADA','ADMINISTRADA','OMITIDA','RECHAZADA','SUSPENDIDA')),
    motivo_omision           varchar(300),
    reaccion_adversa         text,
    observaciones            text,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer
);
```

### Tabla nueva 3.11: `balance_liquidos`

Encabezado del balance de líquidos del paciente.

```sql
CREATE TABLE balance_liquidos (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    sede_id                  integer NOT NULL REFERENCES sede(id),
    atencion_id              integer NOT NULL REFERENCES atencion(id),
    paciente_id              integer NOT NULL REFERENCES paciente(id),
    profesional_id           integer NOT NULL REFERENCES profesional_salud(id),
    fecha_balance            date NOT NULL DEFAULT current_date,
    turno                    varchar(10) CHECK (turno IN ('MANANA','TARDE','NOCHE','DIA_24H')),
    total_ingresos           numeric(10,2) NOT NULL DEFAULT 0,
    total_egresos            numeric(10,2) NOT NULL DEFAULT 0,
    balance                  numeric(10,2) GENERATED ALWAYS AS (total_ingresos - total_egresos) STORED,
    observaciones            text,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer
);
```

### Tabla nueva 3.12: `detalle_balance_liquidos`

```sql
CREATE TABLE detalle_balance_liquidos (
    id                  serial PRIMARY KEY,
    empresa_id          integer NOT NULL REFERENCES empresa(id),
    balance_id          integer NOT NULL REFERENCES balance_liquidos(id),
    tipo                varchar(20) NOT NULL CHECK (tipo IN ('INGRESO','EGRESO')),
    via                 varchar(30) NOT NULL CHECK (via IN
        ('ORAL','IV','SNG','SVD','DRENAJE','VOMITO','DEPOSICION','SUDORACION',
         'INSENSIBLES','OTRO')),
    descripcion         varchar(200),
    cantidad_ml         numeric(10,2) NOT NULL CHECK (cantidad_ml >= 0),
    hora_registro       time NOT NULL,
    activo              boolean NOT NULL DEFAULT true,
    created_at          timestamp NOT NULL DEFAULT current_timestamp,
    deleted_at          timestamp
);
```

### Tabla nueva 3.13: `escala_clinica`

Aplicación de escalas clínicas (Glasgow, EVA, Norton, Morse, Barthel, Downton).

```sql
CREATE TABLE escala_clinica (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    sede_id              integer NOT NULL REFERENCES sede(id),
    atencion_id          integer NOT NULL REFERENCES atencion(id),
    paciente_id          integer NOT NULL REFERENCES paciente(id),
    profesional_id       integer NOT NULL REFERENCES profesional_salud(id),
    tipo_escala          varchar(30) NOT NULL CHECK (tipo_escala IN
        ('GLASGOW','EVA','NORTON','BRADEN','MORSE','DOWNTON','BARTHEL','LAWTON',
         'KATZ','MINI_MENTAL','APGAR','SILVERMAN','GLASGOW_PEDIATRICO','OTRA')),
    fecha_aplicacion     timestamp NOT NULL DEFAULT current_timestamp,
    puntaje_total        integer NOT NULL,
    interpretacion       varchar(200),
    riesgo               varchar(20) CHECK (riesgo IN ('BAJO','MEDIO','ALTO','MUY_ALTO')),
    detalle_escala       jsonb,
    observaciones        text,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer
);
```

`detalle_escala` JSON contiene los ítems individuales de cada escala (varían según el tipo).

### Tabla nueva 3.14: `interconsulta`

Solicitud de interconsulta a otra especialidad y su respuesta.

```sql
CREATE TABLE interconsulta (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    sede_id                  integer NOT NULL REFERENCES sede(id),
    atencion_origen_id       integer NOT NULL REFERENCES atencion(id),
    atencion_respuesta_id    integer REFERENCES atencion(id),
    numero_interconsulta     varchar(30) NOT NULL,
    profesional_solicita_id  integer NOT NULL REFERENCES profesional_salud(id),
    profesional_responde_id  integer REFERENCES profesional_salud(id),
    especialidad_destino_id  integer NOT NULL REFERENCES especialidad(id),
    motivo                   text NOT NULL,
    impresion_diagnostica    text,
    pregunta_clinica         text,
    estado                   varchar(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN
        ('PENDIENTE','EN_PROCESO','RESPONDIDA','ANULADA')),
    prioridad                varchar(20) DEFAULT 'NORMAL' CHECK (prioridad IN
        ('NORMAL','URGENTE','VITAL')),
    fecha_solicitud          timestamp NOT NULL DEFAULT current_timestamp,
    fecha_respuesta          timestamp,
    respuesta                text,
    recomendaciones          text,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT uk_interconsulta UNIQUE (empresa_id, numero_interconsulta)
);
```

### Tabla nueva 3.15: `epicrisis`

Resumen estructurado al egreso hospitalario.

```sql
CREATE TABLE epicrisis (
    id                          serial PRIMARY KEY,
    empresa_id                  integer NOT NULL REFERENCES empresa(id),
    sede_id                     integer NOT NULL REFERENCES sede(id),
    admision_id                 integer NOT NULL REFERENCES admision(id),
    paciente_id                 integer NOT NULL REFERENCES paciente(id),
    profesional_id              integer NOT NULL REFERENCES profesional_salud(id),
    fecha_egreso                timestamp NOT NULL,
    motivo_ingreso              text NOT NULL,
    diagnostico_ingreso         text NOT NULL,
    diagnostico_egreso          text NOT NULL,
    procedimientos_realizados   text,
    evolucion_resumen           text NOT NULL,
    complicaciones              text,
    plan_seguimiento            text NOT NULL,
    medicamentos_egreso         text,
    recomendaciones             text NOT NULL,
    indicaciones_dieta          text,
    indicaciones_actividad      text,
    fecha_proximo_control       date,
    firmada                     boolean NOT NULL DEFAULT false,
    fecha_firma                 timestamp,
    pdf_url                     varchar(500),
    activo                      boolean NOT NULL DEFAULT true,
    created_at                  timestamp NOT NULL DEFAULT current_timestamp,
    updated_at                  timestamp,
    deleted_at                  timestamp,
    usuario_creacion            integer,
    usuario_modificacion        integer,
    CONSTRAINT uk_epicrisis_admision UNIQUE (admision_id)
);
```

### Tabla nueva 3.16: `adjunto_clinico`

Documentos adjuntos a la HC del paciente: fotos, resultados externos, escaneos.

```sql
CREATE TABLE adjunto_clinico (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    sede_id              integer NOT NULL REFERENCES sede(id),
    paciente_id          integer NOT NULL REFERENCES paciente(id),
    atencion_id          integer REFERENCES atencion(id),
    tipo_documento       varchar(30) NOT NULL CHECK (tipo_documento IN
        ('RESULTADO_LABORATORIO','IMAGEN_DIAGNOSTICA','REPORTE_PATOLOGIA',
         'CONSENTIMIENTO','EXAMEN_EXTERNO','FOTO_CLINICA','ECG','OTRO')),
    nombre_archivo       varchar(300) NOT NULL,
    descripcion          varchar(500),
    url_archivo          varchar(500) NOT NULL,
    mime_type            varchar(100),
    tamano_bytes         bigint,
    profesional_carga_id integer REFERENCES profesional_salud(id),
    fecha_documento      date,
    es_confidencial      boolean NOT NULL DEFAULT false,
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer
);
```

### Tabla nueva 3.17: `consentimiento_informado`

Registro del consentimiento informado firmado.

```sql
CREATE TABLE consentimiento_informado (
    id                   serial PRIMARY KEY,
    empresa_id           integer NOT NULL REFERENCES empresa(id),
    sede_id              integer NOT NULL REFERENCES sede(id),
    paciente_id          integer NOT NULL REFERENCES paciente(id),
    atencion_id          integer REFERENCES atencion(id),
    tipo_consentimiento  varchar(50) NOT NULL,
    procedimiento        varchar(300),
    profesional_id       integer NOT NULL REFERENCES profesional_salud(id),
    representante_legal_id integer REFERENCES tercero(id),
    fecha_firma          timestamp NOT NULL DEFAULT current_timestamp,
    contenido            text NOT NULL,
    pdf_url              varchar(500),
    firma_paciente_url   varchar(500),
    activo               boolean NOT NULL DEFAULT true,
    created_at           timestamp NOT NULL DEFAULT current_timestamp,
    updated_at           timestamp,
    deleted_at           timestamp,
    usuario_creacion     integer,
    usuario_modificacion integer
);
```

### Resumen módulo 3
- **Tablas nuevas**: 17 (`tipo_antecedente`, `antecedente_personal`, `antecedente_familiar`, `habito_paciente`, `revision_sistemas`, `vacuna_paciente`, `medicacion_habitual`, `plan_cuidados_enfermeria`, `nota_enfermeria`, `administracion_medicamento`, `balance_liquidos`, `detalle_balance_liquidos`, `escala_clinica`, `interconsulta`, `epicrisis`, `adjunto_clinico`, `consentimiento_informado`)
- **Tablas reutilizadas**: `paciente`, `atencion`, `admision`, `profesional_salud`, `catalogo_diagnostico`, `especialidad`, `via_administracion`, `frecuencia_dosis`, `servicio_salud`, `detalle_prescripcion`, `tercero`, `dispensacion`, `lote`

---

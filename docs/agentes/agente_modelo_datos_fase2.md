# Agente de Modelo de Datos — Fase 2
## Sistema de Gestión Hospitalaria (SGH)

## Rol del agente

Este agente actúa como **Arquitecto de Datos Senior** especializado en la fase 2 del SGH. Su responsabilidad es definir, mantener y validar el modelo de datos relacional que sustenta los 3 nuevos módulos de fase 2:

1. **Glosas y conciliación**
2. **Farmacia e inventario**
3. **Historia clínica avanzada**

El modelo se integra con el esquema `sgh` ya existente (110 tablas de fase 1) y **respeta todas las decisiones arquitectónicas previas**.

---

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

## Módulo 1: Glosas y conciliación

### Contexto del negocio

Cuando una factura se radica ante un pagador, el pagador puede objetar uno o varios ítems (glosar). La institución debe responder, y al final hay una conciliación que define cuánto se acepta como glosa y cuánto se recupera. Las glosas reducen el saldo de la cuenta por cobrar.

La normativa colombiana relevante es la **Resolución 3047 de 2008** (manual de glosas, devoluciones y respuestas) y sus actualizaciones. Cada glosa debe citar un código oficial.

### Tablas existentes en fase 1 (se reutilizan)
- `glosa` — encabezado de glosa por factura
- `detalle_glosa` — glosa por ítem de factura
- `respuesta_glosa` — respuesta de la institución
- `estado_glosa` — catálogo de estados
- `cuenta_por_cobrar` y `movimiento_cuenta_por_cobrar` — para impacto financiero

### Tabla nueva 1.1: `motivo_glosa` (catálogo global)

Catálogo oficial de códigos de glosa según Resolución 3047. Es global porque la norma es nacional.

```sql
CREATE TABLE motivo_glosa (
    id                  serial PRIMARY KEY,
    codigo              varchar(10) NOT NULL UNIQUE,
    nombre              varchar(300) NOT NULL,
    descripcion         text,
    grupo               varchar(50) NOT NULL,
    aplica_devolucion   boolean NOT NULL DEFAULT false,
    activo              boolean NOT NULL DEFAULT true,
    created_at          timestamp NOT NULL DEFAULT current_timestamp,
    updated_at          timestamp,
    deleted_at          timestamp
);
```

**Grupos típicos**:
- FACTURACION (facturación, soportes)
- TARIFAS (tarifas, descuentos)
- COBERTURA (servicios no cubiertos)
- AUTORIZACIONES
- PERTINENCIA (médica, técnica)
- DEVOLUCION (causales de devolución total)

### Tabla nueva 1.2: `concertacion_glosa`

Acuerdo final de conciliación entre la institución y el pagador, con valor aceptado y valor recuperado por la institución.

```sql
CREATE TABLE concertacion_glosa (
    id                       serial PRIMARY KEY,
    empresa_id               integer NOT NULL REFERENCES empresa(id),
    glosa_id                 integer NOT NULL REFERENCES glosa(id),
    fecha_concertacion       date NOT NULL,
    valor_glosa_inicial      numeric(18,2) NOT NULL,
    valor_aceptado_institucion numeric(18,2) NOT NULL DEFAULT 0,
    valor_aceptado_pagador   numeric(18,2) NOT NULL DEFAULT 0,
    valor_recuperado         numeric(18,2) GENERATED ALWAYS AS
        (valor_glosa_inicial - valor_aceptado_institucion) STORED,
    acta_url                 varchar(500),
    observaciones            text,
    activo                   boolean NOT NULL DEFAULT true,
    created_at               timestamp NOT NULL DEFAULT current_timestamp,
    updated_at               timestamp,
    deleted_at               timestamp,
    usuario_creacion         integer,
    usuario_modificacion     integer,
    CONSTRAINT uk_concertacion_glosa UNIQUE (glosa_id)
);
```

### Modificaciones a tablas existentes

`detalle_glosa` debe enlazar al motivo oficial:
```sql
ALTER TABLE detalle_glosa ADD COLUMN motivo_glosa_id integer REFERENCES motivo_glosa(id);
```

### Resumen módulo 1
- **Tablas nuevas**: 2 (`motivo_glosa`, `concertacion_glosa`)
- **Tablas modificadas**: 1 (`detalle_glosa` agrega FK)
- **Tablas reutilizadas**: 5 de fase 1

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

## Resumen total fase 2

| Módulo | Tablas nuevas | Tablas modificadas | Catálogos nuevos |
|--------|--------------|---------------------|-------------------|
| 1 — Glosas | 2 | 1 (`detalle_glosa`) | 1 (`motivo_glosa`) |
| 2 — Farmacia/inventario | 13 | 0 | 0 |
| 3 — HC avanzada | 17 | 0 | 1 (`tipo_antecedente`) |
| **Total** | **32** | **1** | **2** |

Sumado a fase 1: **142 tablas** en total tras fase 2.

---

## Comportamiento del agente

### Qué hace
- Define tablas con todos sus campos, FKs, checks y unicidades.
- Respeta la nomenclatura y reglas heredadas de fase 1.
- Aplica multi-tenant en todas las tablas transaccionales.
- Reutiliza catálogos y tablas existentes antes de crear nuevas.
- Documenta el contexto del negocio de cada módulo.

### Qué NO hace
- No introduce campos en inglés (excepto los 3 de auditoría).
- No crea tablas plurales.
- No omite `empresa_id` en transaccionales.
- No duplica catálogos que ya existen en fase 1.
- No modifica tablas de fase 1 sin justificarlo explícitamente.

### Cuando lo invocas
Cuando una HU de fase 2 necesita una tabla nueva o un campo nuevo, este agente:
1. Verifica si ya existe algo similar en fase 1 o fase 2.
2. Si necesita crear, usa el patrón estándar (PK, FKs, multi-tenant, auditoría, soft-delete).
3. Usa nombres en español snake_case singular.
4. Define unicidades compuestas con `empresa_id`.
5. Define índices parciales para soft-delete en tablas de alto tráfico.

---

## Instrucción final

Este agente es la fuente de verdad del modelo de datos de fase 2. Cualquier discrepancia entre las HUs y las tablas se resuelve a favor de las decisiones documentadas aquí. Si una HU necesita un campo nuevo, se actualiza este documento primero.

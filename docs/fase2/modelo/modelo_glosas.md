# Modelo Glosas

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

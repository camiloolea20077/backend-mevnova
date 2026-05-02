# Regla DDL SGH

Aplicar cuando se modifiquen archivos:
- database/**/*.sql
- sql/**/*.sql
- src/main/resources/db/**/*.sql

Reglas:
- Tablas en español singular snake_case.
- Columnas en español snake_case.
- Auditoría en inglés: created_at, updated_at, deleted_at.
- Toda tabla transaccional usa empresa_id.
- Toda tabla operativa usa sede_id.
- Soft delete con deleted_at.
- Incluir activo boolean NOT NULL DEFAULT true.
- Incluir usuario_creacion y usuario_modificacion.
- Definir FKs.
- Definir CHECK para valores cerrados.
- Definir UNIQUE con empresa_id cuando aplique.
- Tablas de alto tráfico requieren índices parciales WHERE deleted_at IS NULL.
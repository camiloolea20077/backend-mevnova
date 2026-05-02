# Data Architect SGH Lite

Rol:
Arquitecto de datos para SGH.

Usar para:
- Crear tablas.
- Modificar columnas.
- Validar FKs.
- Validar constraints.
- Validar índices.
- Validar auditoría.
- Validar multi-tenant.
- Validar soft delete.
- Revisar si una tabla ya existe.
- Evitar duplicación de catálogos.

Reglas de nomenclatura:
- Tablas en español.
- Singular.
- snake_case.
- Columnas en español snake_case.
- Auditoría en inglés: created_at, updated_at, deleted_at.
- No usar tablas en plural.
- No crear nombres mezclados español/inglés.

Reglas multi-tenant:
- Toda tabla transaccional lleva empresa_id NOT NULL.
- Toda tabla operativa lleva sede_id cuando aplique.
- Catálogos globales no llevan empresa_id.
- Unicidades de negocio deben considerar empresa_id.
- No aceptar empresa_id desde DTO request; se llena desde TenantContext en backend.

Auditoría estándar:
- created_at timestamp NOT NULL DEFAULT current_timestamp.
- updated_at timestamp.
- deleted_at timestamp.
- usuario_creacion integer.
- usuario_modificacion integer.
- activo boolean NOT NULL DEFAULT true.

Soft delete:
- Nunca borrado físico.
- Toda consulta debe filtrar deleted_at IS NULL.
- Tablas de alto tráfico requieren índices parciales WHERE deleted_at IS NULL.

Reglas de diseño:
- Reutilizar tablas de fase 1 antes de crear nuevas.
- No duplicar catálogos existentes.
- No modificar fase 1 sin justificar.
- Si una HU necesita campo nuevo, primero actualizar modelo del módulo.
- Toda FK debe apuntar a tabla existente y coherente.
- Toda tabla con valores cerrados debe tener CHECK o catálogo.

Documentos:
- Glosas: docs/fase2/modelo/modelo_glosas.md
- Farmacia: docs/fase2/modelo/modelo_farmacia.md
- Historia clínica: docs/fase2/modelo/modelo_historia_clinica.md
- Agente completo de referencia: docs/agentes/agente_modelo_datos_fase2.md

Antes de aprobar:
- Tabla en español singular.
- PK definida.
- empresa_id si aplica.
- sede_id si aplica.
- auditoría completa.
- deleted_at.
- activo.
- FKs claras.
- constraints.
- unicidades.
- índices sugeridos.
- compatibilidad con trazabilidad de la HU.
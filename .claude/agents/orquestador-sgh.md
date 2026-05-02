# Orquestador SGH

Rol:
Decidir qué agente y qué documentación consultar para cada tarea, minimizando tokens.

Regla principal:
No cargar todos los documentos. Leer solo lo necesario para la HU activa.

Agentes disponibles:
1. backend-spring-sgh-lite.md
   Para CRUD, servicios, controllers, DTOs, mappers, repositories, QueryRepository y lógica backend.

2. jwt-multitenant-sgh-lite.md
   Solo para autenticación, login, JWT, permisos, TenantContext, SecurityConfig, sesiones, revocación de tokens y aislamiento por token.

3. data-architect-sgh-lite.md
   Solo para validar tablas, columnas, FKs, constraints, índices, auditoría, soft delete y multi-tenant en BD.

4. qa-sgh-lite.md
   Solo para crear o revisar pruebas.

Regla de selección:
- HU funcional normal → backend-spring-sgh-lite.md.
- HU con tabla nueva o campo nuevo → data-architect-sgh-lite.md primero.
- HU con permisos, token, login o TenantContext → jwt-multitenant-sgh-lite.md solo para esa parte.
- Login, refresh, logout, select-sede o change-password → jwt-multitenant-sgh-lite.md como agente principal.
- CRUD simple → no cargar agente JWT completo.
- Pruebas → qa-sgh-lite.md.

Documentos por módulo:
- Glosas:
  - docs/fase2/backlog/bloque_09_glosas.md
  - docs/fase2/modelo/modelo_glosas.md
  - docs/fase2/trazabilidad/trazabilidad_glosas.md

- Farmacia:
  - docs/fase2/backlog/bloque_10_farmacia.md
  - docs/fase2/modelo/modelo_farmacia.md
  - docs/fase2/trazabilidad/trazabilidad_farmacia.md

- Historia clínica:
  - docs/fase2/backlog/bloque_11_historia_clinica.md
  - docs/fase2/modelo/modelo_historia_clinica.md
  - docs/fase2/trazabilidad/trazabilidad_historia_clinica.md

Flujo obligatorio por HU:
1. Leer docs/fase2/00_contexto_minimo_fase2.md.
2. Identificar el bloque por código de HU.
3. Leer solo el archivo de backlog del bloque.
4. Buscar la HU exacta.
5. Leer solo el archivo de trazabilidad del bloque.
6. Buscar la fila exacta de la HU.
7. Leer solo el archivo de modelo del bloque.
8. Buscar las tablas relacionadas.
9. Aplicar el agente correspondiente.
10. No implementar otras HUs.
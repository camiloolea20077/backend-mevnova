# SGH — Memoria mínima Claude Code

Proyecto:
Sistema de Gestión Hospitalaria SGH.

Regla principal:
Trabajar siempre por contexto mínimo. No cargar documentos completos si se puede consultar por HU, módulo, tabla o archivo específico.

Antes de trabajar:
- Leer `.claude/agents/orquestador-sgh.md`.
- Leer `docs/fase2/00_contexto_minimo_fase2.md`.
- Trabajar una sola HU por sesión o por bloque pequeño.
- No implementar funcionalidades fuera de la HU activa.

Stack:
- Backend: Spring Boot 3.x + Java 17.
- BD: PostgreSQL.
- Frontend: Angular 18 + PrimeNG 18 + TailwindCSS.

Reglas backend:
- BD y entidades JPA en español snake_case singular.
- DTOs, services, controllers y variables nuevas en inglés camelCase.
- Auditoría en inglés: created_at, updated_at, deleted_at.
- JPA minimalista: solo save() y findById().
- QueryRepository para filtros, joins, búsqueda, paginación, soft delete y multi-tenant.
- No aceptar empresa_id ni sede_id en DTOs request.
- empresa_id y sede_id salen de TenantContext.
- Soft delete con deleted_at.
- Nunca DELETE físico.
- Controller retorna ApiResponse<T>.
- Mapper con MapStruct.
- No usar @Data.
- No usar @Autowired en campos.

Regla de archivos request por HU:
- Cada HU implementada debe crear un archivo `.http` para pruebas manuales siguiendo el estilo usado en el proyecto SGH.
- Los archivos `.http` deben guardarse en `docs/fase2/requests/` según el módulo.
- Glosas: `docs/fase2/requests/glosas/`
- Farmacia: `docs/fase2/requests/farmacia/`
- Historia clínica: `docs/fase2/requests/historia_clinica/`
- El archivo debe llamarse `HU-FASE2-XXX_nombre_corto.http`.

Formato obligatorio de los archivos `.http`:
- Usar `@port = 9001`.
- Usar `@token = Bearer PEGAR_TOKEN_AQUI`.
- Usar `@base = http://localhost:{{port}}/api/<endpoint>`.
- Separar cada request con `###`.
- Cada bloque debe iniciar con comentario de HU y descripción.
- Usar `Authorization: {{token}}`.
- Usar `Content-Type: application/json` cuando el request tenga body.
- Para listados paginados usar preferiblemente `POST {{base}}/list`.
- El body de listado debe usar:
  - `page`
  - `rows`
  - `search`
  - `order_by`
  - `order`
- Para cambios de estado usar `PATCH` cuando aplique.
- No incluir tokens reales.
- No incluir datos sensibles reales.
- Usar variables claras como `@bodegaId`, `@pacienteId`, `@glosaId`, `@sedeId`.

Optimización de memoria:
- No cargar agentes completos salvo necesidad.
- Usar agentes lite por defecto.
- Usar documentación modular por fase/módulo.
- Usar `/clear` entre HUs no relacionadas.
- Usar `/compact` al terminar una HU larga.
- Usar subagentes para investigar sin llenar el contexto principal.

Agentes:
- Orquestador: `.claude/agents/orquestador-sgh.md`
- Backend: `.claude/agents/backend-spring-sgh-lite.md`
- Seguridad: `.claude/agents/jwt-multitenant-sgh-lite.md`
- Datos: `.claude/agents/data-architect-sgh-lite.md`
- QA: `.claude/agents/qa-sgh-lite.md`

Documentación fase 2:
- Contexto mínimo: `docs/fase2/00_contexto_minimo_fase2.md`
- Backlog modular: `docs/fase2/backlog/`
- Modelo modular: `docs/fase2/modelo/`
- Trazabilidad modular: `docs/fase2/trazabilidad/`
- Requests por HU: `docs/fase2/requests/`

Cuando se compacte:
Preservar siempre:
- HU activa.
- Archivos modificados.
- Decisiones técnicas tomadas.
- Errores encontrados.
- Tests ejecutados.
- Archivo `.http` creado.
- Pendientes.
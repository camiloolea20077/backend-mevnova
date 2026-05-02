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

Cuando se compacte:
Preservar siempre:
- HU activa.
- Archivos modificados.
- Decisiones técnicas tomadas.
- Errores encontrados.
- Tests ejecutados.
- Pendientes.
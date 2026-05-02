# Revisar seguridad SGH

Objetivo:
Revisar únicamente seguridad, permisos y aislamiento multi-tenant.

Usar:
- `.claude/agents/jwt-multitenant-sgh-lite.md`
- `.claude/agents/backend-spring-sgh-lite.md`

Validar:
1. No se aceptan empresa_id ni sede_id en DTO request.
2. empresa_id sale de TenantContext.getEmpresaId().
3. sede_id sale de TenantContext.getSedeId() cuando aplica.
4. usuario_creacion y usuario_modificacion salen de TenantContext.getUsuarioId().
5. QueryRepository filtra empresa_id.
6. QueryRepository filtra sede_id en tablas operativas.
7. QueryRepository filtra deleted_at IS NULL.
8. No hay fuga cross-tenant.
9. Por ID cross-tenant responde 404.
10. Falta de permiso responde 403.
11. Token faltante o inválido responde 401.
12. Endpoints tienen permisos si la HU lo exige.
13. No hay DELETE físico.
14. No hay secrets hardcodeados.
15. No hay mensajes que revelen existencia de usuario, empresa o recurso sensible.

Entrega:
- Hallazgos críticos.
- Hallazgos medios.
- Recomendaciones.
- Cambios propuestos.
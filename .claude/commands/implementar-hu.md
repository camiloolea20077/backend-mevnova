# Implementar HU SGH

Entrada:
Código de HU, ejemplo: HU-FASE2-067.

Objetivo:
Implementar únicamente la HU indicada, usando contexto mínimo.

Proceso obligatorio:
1. Leer `.claude/agents/orquestador-sgh.md`.
2. Leer `docs/fase2/00_contexto_minimo_fase2.md`.
3. Identificar módulo:
   - HU-FASE2-061 a 066 = Glosas.
   - HU-FASE2-067 a 078 = Farmacia.
   - HU-FASE2-079 a 093 = Historia clínica.
4. Leer solo el backlog del módulo.
5. Buscar la HU exacta.
6. Leer solo la trazabilidad del módulo.
7. Buscar la fila exacta de la HU.
8. Leer solo el modelo del módulo.
9. Buscar solo las tablas relacionadas.
10. Usar `backend-spring-sgh-lite.md`.
11. Usar `data-architect-sgh-lite.md` si hay tabla/campo nuevo.
12. Usar `jwt-multitenant-sgh-lite.md` solo si la tarea toca permisos, autenticación, token o TenantContext.
13. No implementar otras HUs.

Antes de modificar:
- Inspeccionar estructura existente del proyecto.
- Buscar un módulo similar ya implementado.
- Seguir el patrón existente.
- Proponer plan corto.
- Implementar por capas.

Capas esperadas:
- Entity.
- DTO request.
- DTO response.
- DTO table/list si aplica.
- Mapper.
- JpaRepository vacío.
- QueryRepository.
- Service interface.
- ServiceImpl.
- Controller.
- Validaciones.
- Tests básicos.

Reglas críticas:
- No aceptar empresa_id ni sede_id en request.
- Usar TenantContext.
- Filtrar deleted_at IS NULL.
- Validar empresa_id.
- Validar sede_id si aplica.
- Responder 404 para cross-tenant por ID.
- Responder 403 para falta de permiso.
- No usar DELETE físico.
- No usar métodos JPA derivados largos.

Entrega final:
- Lista de archivos modificados.
- Resumen de implementación.
- Validaciones cubiertas.
- Pruebas creadas o sugeridas.
- Riesgos pendientes si existen.
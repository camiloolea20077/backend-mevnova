# Memoria de trabajo — Fase 2 SGH

Este archivo resume decisiones estables del proyecto. No contiene backlog completo ni código generado.

## Decisiones permanentes

- Fase 2 está dividida en:
  - Glosas y conciliación.
  - Farmacia e inventario.
  - Historia clínica avanzada.

- Trabajar una HU por vez.

- Usar `/implementar-hu HU-FASE2-XXX` para implementación completa.

- Usar `/crear-queryrepository` solo si se quiere trabajar únicamente la capa de consultas.

- Usar `/revisar-seguridad` después de implementar una HU sensible.

- Usar `/crear-tests` al finalizar la HU.

## Reglas backend

- JPA minimalista.
- QueryRepository para filtros y consultas.
- DTOs en inglés camelCase.
- Entidades en español snake_case.
- No aceptar empresa_id ni sede_id en request.
- Usar TenantContext.
- Soft delete con deleted_at.

## Regla de seguridad

- 401: token faltante, inválido o vencido.
- 403: usuario autenticado sin permiso.
- 404: recurso inexistente o cross-tenant por ID.

## Regla de documentación

- Backlog modular en `docs/fase2/backlog/`.
- Modelo modular en `docs/fase2/modelo/`.
- Trazabilidad modular en `docs/fase2/trazabilidad/`.

## Flujo por HU

1. Leer contexto mínimo.
2. Leer backlog del módulo.
3. Buscar HU exacta.
4. Leer trazabilidad del módulo.
5. Buscar fila exacta.
6. Leer modelo del módulo.
7. Buscar tablas relacionadas.
8. Implementar.
9. Revisar seguridad.
10. Crear tests.
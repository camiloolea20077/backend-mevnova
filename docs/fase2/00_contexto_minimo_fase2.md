# Contexto mínimo — Fase 2 SGH

## Propósito

Este archivo sirve como punto de entrada liviano para Claude Code. Su objetivo es evitar cargar documentos grandes completos cuando solo se va a trabajar una historia de usuario, tabla o módulo específico.

## Alcance de fase 2

La fase 2 del Sistema de Gestión Hospitalaria SGH incorpora tres módulos:

1. **Glosas y conciliación**.
2. **Farmacia e inventario**.
3. **Historia clínica avanzada**.

## Resumen cuantitativo

- **33 historias de usuario**: HU-FASE2-061 a HU-FASE2-093.
- **32 tablas nuevas**.
- **1 tabla modificada**: `detalle_glosa`.
- **2 catálogos nuevos**: `motivo_glosa` y `tipo_antecedente`.
- **6 sprints planificados**: Sprint 8 a Sprint 13.
- **Total acumulado posterior a fase 2**: 142 tablas.

## Mapa de módulos

| Bloque | Módulo | Historias | Documentos principales |
|---|---|---|---|
| 9 | Glosas y conciliación | HU-FASE2-061 a HU-FASE2-066 | `backlog/bloque_09_glosas.md`, `modelo/modelo_glosas.md`, `trazabilidad/trazabilidad_glosas.md` |
| 10 | Farmacia e inventario | HU-FASE2-067 a HU-FASE2-078 | `backlog/bloque_10_farmacia.md`, `modelo/modelo_farmacia.md`, `trazabilidad/trazabilidad_farmacia.md` |
| 11 | Historia clínica avanzada | HU-FASE2-079 a HU-FASE2-093 | `backlog/bloque_11_historia_clinica.md`, `modelo/modelo_historia_clinica.md`, `trazabilidad/trazabilidad_historia_clinica.md` |

## Reglas técnicas heredadas

- Multi-tenant obligatorio con `empresa_id`.
- Tablas operativas con `sede_id` cuando aplique.
- Soft delete universal con `deleted_at`.
- Toda consulta funcional debe filtrar `deleted_at IS NULL`.
- Auditoría estándar con `created_at`, `updated_at`, `deleted_at`, `usuario_creacion`, `usuario_modificacion`, `activo`.
- Base de datos en español, singular y `snake_case`.
- Entidades JPA en español y `snake_case`, alineadas con la base de datos.
- DTOs en inglés y `camelCase`.
- Mappers traducen explícitamente EN ↔ ES.
- No aceptar `empresa_id` ni `sede_id` desde DTOs de request; se toman desde `TenantContext`.
- Reutilizar tablas y catálogos existentes antes de crear nuevos.
- No modificar fase 1 sin justificación explícita.

## Forma de trabajo optimizada

Para implementar una HU:

1. Leer este archivo.
2. Identificar el bloque por el código de HU.
3. Abrir solo el archivo de backlog del módulo correspondiente.
4. Buscar únicamente la HU solicitada.
5. Abrir solo el archivo de trazabilidad del módulo correspondiente.
6. Buscar únicamente la fila de esa HU.
7. Abrir solo el modelo del módulo correspondiente.
8. Consultar únicamente las tablas relacionadas.
9. Usar el agente lite correspondiente:
   - Backend: `.claude/agents/backend-spring-sgh-lite.md`.
   - Seguridad/JWT: `.claude/agents/jwt-multitenant-sgh-lite.md`.
   - Datos: `.claude/agents/data-architect-sgh-lite.md`.
   - QA: `.claude/agents/qa-sgh-lite.md`.

## Regla de oro para ahorrar tokens

No cargar documentos completos si se puede trabajar por:

- Código de HU.
- Nombre de tabla.
- Módulo.
- Criterio de aceptación.
- Archivo específico.

## Qué NO debe cargarse por defecto

- Todo el backlog de fase 2.
- Todo el modelo de datos de fase 2.
- Toda la matriz de trazabilidad.
- Todo el agente JWT completo.
- Todo el agente backend completo.

Los documentos completos quedan como referencia en `docs/agentes`, pero el trabajo diario debe hacerse con archivos modulares y agentes lite.

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

Documentos por módulo:
- Glosas:
  - Backlog: `docs/fase2/backlog/bloque_09_glosas.md`
  - Modelo: `docs/fase2/modelo/modelo_glosas.md`
  - Trazabilidad: `docs/fase2/trazabilidad/trazabilidad_glosas.md`
  - Requests: `docs/fase2/requests/glosas/`

- Farmacia:
  - Backlog: `docs/fase2/backlog/bloque_10_farmacia.md`
  - Modelo: `docs/fase2/modelo/modelo_farmacia.md`
  - Trazabilidad: `docs/fase2/trazabilidad/trazabilidad_farmacia.md`
  - Requests: `docs/fase2/requests/farmacia/`

- Historia clínica:
  - Backlog: `docs/fase2/backlog/bloque_11_historia_clinica.md`
  - Modelo: `docs/fase2/modelo/modelo_historia_clinica.md`
  - Trazabilidad: `docs/fase2/trazabilidad/trazabilidad_historia_clinica.md`
  - Requests: `docs/fase2/requests/historia_clinica/`

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
- Archivo `.http` de pruebas manuales.

Reglas críticas:
- No aceptar empresa_id ni sede_id en request.
- Usar TenantContext.
- Filtrar deleted_at IS NULL.
- Validar empresa_id.
- Validar sede_id si aplica.
- Responder 404 para cross-tenant por ID.
- Responder 403 para falta de permiso.
- Responder 401 para token faltante, inválido o vencido.
- No usar DELETE físico.
- No usar métodos JPA derivados largos.
- No implementar otras HUs.

## Archivo `.http` obligatorio

Para cada HU implementada, crear un archivo `.http` para pruebas manuales siguiendo el estilo usado por el proyecto SGH.

Ubicación según módulo:
- Glosas: `docs/fase2/requests/glosas/`
- Farmacia: `docs/fase2/requests/farmacia/`
- Historia clínica: `docs/fase2/requests/historia_clinica/`

Nombre del archivo:
- `HU-FASE2-XXX_nombre_corto.http`

Formato base obligatorio:

```http
@port = 9001
@token = Bearer PEGAR_TOKEN_AQUI

@base = http://localhost:{{port}}/api/<endpoint>

### HU-FASE2-XXX — Flujo principal
POST {{base}}
Content-Type: application/json
Authorization: {{token}}

{
  "campo": "valor"
}

###

### HU-FASE2-XXX — Consultar por ID
GET {{base}}/1
Authorization: {{token}}

###

### HU-FASE2-XXX — Listar registros activos
POST {{base}}/list
Content-Type: application/json
Authorization: {{token}}

{
  "page": 0,
  "rows": 10,
  "search": "",
  "order_by": "id",
  "order": "DESC"
}

###
```

El archivo `.http` debe incluir, según aplique:
1. Flujo exitoso principal.
2. Consulta por ID.
3. Listado paginado con `POST {{base}}/list`.
4. Búsqueda con `search`.
5. Actualización.
6. Cambio de estado con `PATCH`.
7. Eliminación lógica.
8. Validación negativa por campo obligatorio.
9. Validación negativa por ID inexistente.
10. Validación negativa por regla de negocio.

Reglas del archivo `.http`:
- No incluir tokens reales.
- No incluir datos sensibles reales.
- Usar `@token = Bearer PEGAR_TOKEN_AQUI`.
- Usar `Authorization: {{token}}`.
- Usar `Content-Type: application/json` solo cuando haya body.
- Usar `###` para separar bloques.
- El comentario de cada bloque debe indicar la HU y qué se está probando.
- Respetar la ruta real implementada en el Controller.
- Para listados, usar preferiblemente `POST {{base}}/list`.
- Para acciones de cambio de estado, usar `PATCH` cuando aplique.
- Usar variables para IDs:
  - `@bodegaId`
  - `@proveedorId`
  - `@compraId`
  - `@loteId`
  - `@glosaId`
  - `@pacienteId`
  - `@atencionId`
  - etc.

Entrega final:
- Lista de archivos modificados.
- Resumen de implementación.
- Validaciones cubiertas.
- Pruebas creadas o sugeridas.
- Archivo `.http` creado para probar la HU.
- Ruta exacta del archivo `.http`.
- Riesgos pendientes si existen.

Formato esperado de la entrega final:

```text
HU implementada: HU-FASE2-XXX Nombre de la HU

Archivos creados/modificados:
- ...
- docs/fase2/requests/<modulo>/HU-FASE2-XXX_nombre_corto.http

Validaciones cubiertas:
- Multi-tenant.
- Soft delete.
- Permisos.
- Reglas de negocio.

Archivo de pruebas:
- Ruta: docs/fase2/requests/<modulo>/HU-FASE2-XXX_nombre_corto.http
- Contiene: flujo exitoso, listado/consulta, actualización, eliminación lógica y validaciones negativas.

Pendientes:
- ...
```
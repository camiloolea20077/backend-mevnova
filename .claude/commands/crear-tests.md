# Crear tests SGH

Objetivo:
Crear pruebas para una HU específica y validar que exista el archivo `.http` de pruebas manuales.

Usar:
- `.claude/agents/qa-sgh-lite.md`
- `.claude/agents/backend-spring-sgh-lite.md`
- Backlog del módulo.
- Trazabilidad de la HU.
- Modelo de tablas relacionadas.

Entrada:
Código de HU.

Proceso:
1. Leer la HU exacta.
2. Leer criterios de aceptación.
3. Leer trazabilidad.
4. Identificar tablas principales.
5. Identificar endpoints implementados.
6. Crear o proponer pruebas mínimas.
7. Verificar que exista archivo `.http` de la HU.
8. Si no existe, crearlo en la carpeta correspondiente.
9. Si existe pero tiene token real, reemplazarlo por `Bearer PEGAR_TOKEN_AQUI`.

Ubicación del archivo `.http`:
- HU-FASE2-061 a HU-FASE2-066:
  - `docs/fase2/requests/glosas/`

- HU-FASE2-067 a HU-FASE2-078:
  - `docs/fase2/requests/farmacia/`

- HU-FASE2-079 a HU-FASE2-093:
  - `docs/fase2/requests/historia_clinica/`

Nombre del archivo:
- `HU-FASE2-XXX_nombre_corto.http`

Cobertura mínima de tests:
- Flujo exitoso.
- Campos obligatorios.
- Validaciones de negocio.
- Multi-tenant.
- Sede si aplica.
- Permisos.
- Soft delete.
- Auditoría.
- Error por recurso no encontrado.
- Error por cross-tenant.
- Rollback si hay transacción.

## Validación del archivo `.http`

Además de los tests unitarios o de integración, verificar que exista un archivo `.http` para la HU.

Formato obligatorio:
- `@port = 9001`
- `@token = Bearer PEGAR_TOKEN_AQUI`
- `@base = http://localhost:{{port}}/api/<endpoint>`
- Bloques separados por `###`
- Comentarios por HU.
- `Authorization: {{token}}`
- `Content-Type: application/json` cuando aplique.
- Listados con `POST {{base}}/list` si el Controller usa ese patrón.
- Body de listado con:
  - `page`
  - `rows`
  - `search`
  - `order_by`
  - `order`

Cobertura mínima del archivo `.http`:
1. Variables:
   - `@port`
   - `@token`
   - `@base`
   - IDs necesarios.
2. Flujo exitoso principal.
3. Listado o consulta.
4. Consulta por ID si aplica.
5. Actualización si aplica.
6. Cambio de estado con `PATCH` si aplica.
7. Eliminación lógica si aplica.
8. Validación negativa.
9. Recurso inexistente.
10. Comentarios claros.

Reglas:
- No incluir tokens reales.
- No incluir datos sensibles reales.
- Usar placeholders.
- Los requests deben ser compatibles con REST Client de VS Code o IntelliJ HTTP Client.
- Si el endpoint real usa otra ruta, respetar la ruta implementada en el Controller.
- Si la HU no tiene CRUD completo, crear solo los requests que correspondan al flujo real.
- Si el archivo `.http` no existe, crearlo.
- Si existe pero tiene token real, reemplazarlo por `Bearer PEGAR_TOKEN_AQUI`.

Entrega:
- Tests unitarios sugeridos o creados.
- Tests de integración sugeridos o creados.
- Casos funcionales manuales.
- Datos mínimos de prueba.
- Ruta del archivo `.http`.
- Validaciones incluidas en el archivo `.http`.

Formato de entrega:

```text
Pruebas HU-FASE2-XXX

Tests creados/sugeridos:
- ...

Archivo request:
- docs/fase2/requests/<modulo>/HU-FASE2-XXX_nombre_corto.http

El archivo incluye:
- Flujo exitoso.
- Consulta/listado.
- Actualización si aplica.
- Cambio de estado si aplica.
- Eliminación lógica si aplica.
- Validaciones negativas.
- Recurso inexistente.
```
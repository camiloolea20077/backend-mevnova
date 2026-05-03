# Backend Spring SGH Lite

Rol:
Desarrollador Backend Senior para el Sistema de Gestión Hospitalaria SGH.

Usar para:
- CRUD backend.
- Services.
- Controllers.
- DTOs.
- Mappers.
- JpaRepository.
- QueryRepository.
- Validaciones de negocio.
- Pruebas backend básicas.
- Archivos `.http` de prueba por HU.

Stack:
- Java 17.
- Spring Boot 3.x.
- PostgreSQL.
- Spring Data JPA mínimo.
- NamedParameterJdbcTemplate para SQL nativo.
- MapStruct.
- Lombok solo con @Getter y @Setter.
- ApiResponse<T> como respuesta estándar.

Reglas de idioma:
- Tablas y columnas BD: español snake_case singular.
- Entidades JPA: español snake_case igual que BD.
- DTOs: inglés camelCase.
- Servicios, controllers y variables nuevas: inglés.
- Mensajes de error: español.
- Auditoría: created_at, updated_at, deleted_at.

Regla JPA:
- JpaRepository debe estar vacío.
- Solo usar save() y findById().
- No crear métodos derivados largos.
- No usar @Query largo dentro de JpaRepository.

Regla QueryRepository:
Usar QueryRepository para:
- Listados.
- Paginación.
- Búsquedas.
- exists.
- Joins.
- Consultas con empresa_id.
- Consultas con sede_id.
- Consultas con deleted_at IS NULL.
- Consultas complejas.

Multi-tenant:
- Nunca recibir empresa_id ni sede_id en DTO request.
- Al crear, empresa_id sale de TenantContext.getEmpresaId().
- Al crear registros operativos, sede_id sale de TenantContext.getSedeId().
- Al consultar, filtrar por empresa_id.
- En tablas operativas, filtrar también por sede_id.
- Si un registro existe pero pertenece a otra empresa, responder 404 como “No encontrado”.

Soft delete:
- Nunca DELETE físico.
- Eliminar = set deleted_at = LocalDateTime.now().
- También set usuario_modificacion desde TenantContext.getUsuarioId().
- Toda consulta debe filtrar deleted_at IS NULL.

Auditoría:
- Al crear: usuario_creacion = TenantContext.getUsuarioId().
- Al modificar: usuario_modificacion = TenantContext.getUsuarioId().
- created_at se setea en @PrePersist.
- updated_at se setea en @PreUpdate.

Controller:
- Retornar ApiResponse<T>.
- No poner lógica de negocio en controller.
- No usar try/catch innecesario.
- Usar validaciones con jakarta.validation.
- Aplicar permisos si la HU lo exige.

Mapper:
- Usar MapStruct componentModel = "spring".
- Traducir explícitamente EN ↔ ES.
- Ignorar id, empresa_id, sede_id, auditoría y deleted_at en creación desde request.

## Archivo request por HU

Cada HU implementada debe generar un archivo `.http` de prueba manual usando el estilo real del proyecto SGH.

Ubicación:
- Glosas: `docs/fase2/requests/glosas/`
- Farmacia: `docs/fase2/requests/farmacia/`
- Historia clínica: `docs/fase2/requests/historia_clinica/`

Nombre:
- `HU-FASE2-XXX_nombre_corto.http`

Formato obligatorio:

```http
@port = 9001
@token = Bearer PEGAR_TOKEN_AQUI

@base = http://localhost:{{port}}/api/<endpoint>

### HU-FASE2-XXX — Nombre del caso principal
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

Contenido mínimo del archivo `.http`:
1. Variables:
   - `@port`
   - `@token`
   - `@base`
2. Request de creación o flujo principal.
3. Request de consulta por ID si aplica.
4. Request de listado usando `POST {{base}}/list` si existe endpoint de listado.
5. Request de búsqueda usando `search`.
6. Request de actualización si aplica.
7. Request de cambio de estado con `PATCH` si aplica.
8. Request de eliminación lógica si aplica.
9. Casos negativos:
   - campo obligatorio faltante.
   - ID inexistente.
   - validación de negocio.

Reglas del archivo `.http`:
- No incluir tokens reales.
- Usar siempre `@token = Bearer PEGAR_TOKEN_AQUI`.
- No incluir datos sensibles reales.
- Respetar las rutas reales implementadas en el Controller.
- Usar `Authorization: {{token}}`.
- Usar `Content-Type: application/json` solo si el request tiene body.
- Usar `###` para separar cada bloque.
- Cada bloque debe iniciar con comentario de HU y descripción.
- Usar nombres de variables claros: `@bodegaId`, `@pacienteId`, `@glosaId`, `@sedeId`, `@proveedorId`, etc.
- Para listados usar preferiblemente `POST {{base}}/list` con body paginado.
- Para cambios de estado usar `PATCH` cuando aplique.

Prohibido:
- @Data.
- @Autowired en campos.
- DELETE físico.
- empresa_id o sede_id en request.
- Repositorios JPA con métodos derivados largos.
- Implementar funcionalidades fuera de la HU.
- Cargar el agente JWT completo para CRUD simple.
- Crear archivos `.http` con tokens reales.
- Crear archivos `.http` con datos reales sensibles.

Antes de entregar:
- Entity creada o ajustada.
- DTOs creados.
- Mapper creado.
- JpaRepository vacío.
- QueryRepository con filtros.
- Service con transacciones.
- Controller con ApiResponse.
- Validación multi-tenant.
- Soft delete.
- Archivo `.http` creado.
- Pruebas o casos de prueba básicos.
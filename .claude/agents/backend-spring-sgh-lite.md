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

Prohibido:
- @Data.
- @Autowired en campos.
- DELETE físico.
- empresa_id o sede_id en request.
- Repositorios JPA con métodos derivados largos.
- Implementar funcionalidades fuera de la HU.
- Cargar el agente JWT completo para CRUD simple.

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
- Pruebas o casos de prueba básicos.
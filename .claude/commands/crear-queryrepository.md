# Crear QueryRepository SGH

Objetivo:
Crear o ajustar únicamente el QueryRepository solicitado.

Usar:
- `.claude/agents/backend-spring-sgh-lite.md`
- Trazabilidad de la HU.
- Modelo de datos de las tablas relacionadas.

Reglas:
- Usar NamedParameterJdbcTemplate.
- Usar SQL nativo.
- Retornar DTOs, no entidades.
- No usar métodos derivados JPA.
- No usar @Query en JpaRepository.
- Filtrar deleted_at IS NULL.
- Filtrar empresa_id = TenantContext.getEmpresaId().
- Filtrar sede_id = TenantContext.getSedeId() si la tabla es operativa.
- Usar COUNT(*) OVER() para paginación cuando aplique.
- Usar MapSqlParameterSource.
- Usar MapperRepository.mapListToDtoList si existe en el proyecto.

Debe incluir métodos según necesidad:
- findActiveById.
- list/paginate.
- exists.
- validateOwnership.
- búsquedas por texto.
- consultas auxiliares de catálogos.

Entrega:
- Código del QueryRepository.
- SQL explicado brevemente.
- Índices recomendados si la consulta lo requiere.
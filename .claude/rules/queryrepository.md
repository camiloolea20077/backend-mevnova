# Regla QueryRepository SGH

Aplicar cuando se modifiquen archivos:
- src/main/java/**/repository/**/*QueryRepository.java

Reglas:
- Usar NamedParameterJdbcTemplate.
- Usar SQL nativo.
- Retornar DTOs, no entidades.
- No usar métodos derivados JPA.
- Siempre filtrar deleted_at IS NULL.
- Siempre filtrar empresa_id cuando aplique.
- Filtrar sede_id en tablas operativas.
- Usar MapSqlParameterSource.
- Usar COUNT(*) OVER() para paginación si aplica.
- Usar MapperRepository.mapListToDtoList si existe.
- No construir SQL inseguro con concatenación de datos del usuario.
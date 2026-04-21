package com.cloud_tecnological.mednova.repositories.rol;

import com.cloud_tecnological.mednova.dto.permiso.PermisoDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PermisoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PermisoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<PermisoDto> listAll() {
        String sql = """
                SELECT id, codigo, nombre, descripcion, modulo, activo
                FROM permiso
                WHERE activo = true
                  AND deleted_at IS NULL
                ORDER BY modulo, nombre
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource(), new ColumnMapRowMapper());
        return rows.stream().map(this::mapRowToDto).toList();
    }

    public PageImpl<PermisoDto> listPaged(PageableDto<?> pageable) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT id, codigo, nombre, descripcion, modulo, activo,
                       COUNT(*) OVER() AS total_rows
                FROM permiso
                WHERE deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (search != null && !search.isEmpty()) {
            sql.append("AND (UPPER(nombre) LIKE UPPER(:search) OR UPPER(modulo) LIKE UPPER(:search)) ");
            params.addValue("search", "%" + search + "%");
        }

        sql.append(" ORDER BY modulo, nombre OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<PermisoDto> dtos = result.stream().map(this::mapRowToDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private PermisoDto mapRowToDto(Map<String, Object> row) {
        return PermisoDto.builder()
                .id(((Number) row.get("id")).longValue())
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .description((String) row.get("descripcion"))
                .module((String) row.get("modulo"))
                .active((Boolean) row.get("activo"))
                .build();
    }
}

package com.cloud_tecnological.mednova.repositories.recursofisico;

import com.cloud_tecnological.mednova.dto.recursofisico.RecursoFisicoResponseDto;
import com.cloud_tecnological.mednova.dto.recursofisico.RecursoFisicoTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class RecursoFisicoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RecursoFisicoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsByCodigo(String codigo, Long sedeId, Long empresaId, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM recurso_fisico"
                + " WHERE UPPER(codigo) = UPPER(:codigo) AND sede_id = :sede_id AND empresa_id = :empresa_id"
                + " AND deleted_at IS NULL AND id != :exclude_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("codigo", codigo)
                .addValue("sede_id", sedeId)
                .addValue("empresa_id", empresaId)
                .addValue("exclude_id", excludeId == null ? -1L : excludeId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsActivo(Long id, Long sedeId, Long empresaId) {
        String sql = "SELECT COUNT(*) FROM recurso_fisico"
                + " WHERE id = :id AND sede_id = :sede_id AND empresa_id = :empresa_id"
                + " AND activo = true AND deleted_at IS NULL";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("sede_id", sedeId)
                .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public Optional<RecursoFisicoResponseDto> findActiveById(Long id, Long empresaId, Long sedeId) {
        String sql = """
            SELECT id, codigo, nombre, tipo_recurso, ubicacion, descripcion, activo, created_at, updated_at
            FROM recurso_fisico
            WHERE id = :id AND empresa_id = :empresa_id AND sede_id = :sede_id AND deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresaId)
                .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponse(rows.get(0)));
    }

    public PageImpl<RecursoFisicoTableDto> listActivos(PageableDto<?> pageable, Long empresaId, Long sedeId) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT id, codigo, nombre, tipo_recurso, ubicacion, activo,
                   COUNT(*) OVER() AS total_rows
            FROM recurso_fisico
            WHERE empresa_id = :empresa_id AND sede_id = :sede_id AND deleted_at IS NULL
            """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresaId)
                .addValue("sede_id", sedeId);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (UPPER(codigo) LIKE UPPER(:search) OR UPPER(nombre) LIKE UPPER(:search))");
            params.addValue("search", "%" + search + "%");
        }

        java.util.Set<String> allowed = java.util.Set.of("codigo", "nombre", "tipo_recurso", "activo", "created_at");
        String rawCol = pageable.getOrder_by() != null ? pageable.getOrder_by() : "nombre";
        String col = rawCol.contains(".") ? rawCol.substring(rawCol.lastIndexOf('.') + 1) : rawCol;
        String orderBy = allowed.contains(col) ? col : "nombre";
        String order = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<RecursoFisicoTableDto> dtos = result.stream().map(this::mapToTable).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private RecursoFisicoResponseDto mapToResponse(Map<String, Object> row) {
        RecursoFisicoResponseDto dto = new RecursoFisicoResponseDto();
        dto.setId(toLong(row.get("id")));
        dto.setCodigo((String) row.get("codigo"));
        dto.setNombre((String) row.get("nombre"));
        dto.setTipo_recurso((String) row.get("tipo_recurso"));
        dto.setUbicacion((String) row.get("ubicacion"));
        dto.setDescripcion((String) row.get("descripcion"));
        dto.setActivo((Boolean) row.get("activo"));
        dto.setCreated_at(toLocalDateTime(row.get("created_at")));
        dto.setUpdated_at(toLocalDateTime(row.get("updated_at")));
        return dto;
    }

    private RecursoFisicoTableDto mapToTable(Map<String, Object> row) {
        RecursoFisicoTableDto dto = new RecursoFisicoTableDto();
        dto.setId(toLong(row.get("id")));
        dto.setCodigo((String) row.get("codigo"));
        dto.setNombre((String) row.get("nombre"));
        dto.setTipo_recurso((String) row.get("tipo_recurso"));
        dto.setUbicacion((String) row.get("ubicacion"));
        dto.setActivo((Boolean) row.get("activo"));
        return dto;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

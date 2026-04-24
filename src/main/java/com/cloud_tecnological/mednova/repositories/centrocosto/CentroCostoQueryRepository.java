package com.cloud_tecnological.mednova.repositories.centrocosto;

import com.cloud_tecnological.mednova.dto.centrocosto.CentroCostoResponseDto;
import com.cloud_tecnological.mednova.dto.centrocosto.CentroCostoTableDto;
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
public class CentroCostoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CentroCostoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<CentroCostoResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT cc.id,
                       cc.empresa_id,
                       cc.codigo,
                       cc.nombre,
                       cc.centro_costo_padre_id,
                       cp.nombre AS padre_nombre,
                       cc.descripcion,
                       cc.activo,
                       cc.created_at
                FROM centro_costo cc
                LEFT JOIN centro_costo cp ON cp.id = cc.centro_costo_padre_id AND cp.deleted_at IS NULL
                WHERE cc.id = :id
                  AND cc.empresa_id = :empresa_id
                  AND cc.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public boolean existsByCode(String codigo, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM centro_costo
                WHERE codigo = :codigo
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("codigo", codigo)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    public boolean hasActiveChildren(Long id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM centro_costo
                WHERE centro_costo_padre_id = :id
                  AND empresa_id = :empresa_id
                  AND activo = true
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<CentroCostoTableDto> listCentrosCosto(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT cc.id,
                       cc.codigo,
                       cc.nombre,
                       cp.nombre AS padre_nombre,
                       cc.activo,
                       COUNT(*) OVER() AS total_rows
                FROM centro_costo cc
                LEFT JOIN centro_costo cp ON cp.id = cc.centro_costo_padre_id AND cp.deleted_at IS NULL
                WHERE cc.empresa_id = :empresa_id
                  AND cc.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        unaccent(UPPER(cc.nombre)) LIKE unaccent(UPPER(:search))
                        OR UPPER(cc.codigo) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "cc.nombre";
        String order = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<CentroCostoTableDto> dtos = result.stream().map(this::mapToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private CentroCostoResponseDto mapToResponseDto(Map<String, Object> row) {
        return CentroCostoResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .parentId(toLong(row.get("centro_costo_padre_id")))
                .parentName((String) row.get("padre_nombre"))
                .description((String) row.get("descripcion"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private CentroCostoTableDto mapToTableDto(Map<String, Object> row) {
        return CentroCostoTableDto.builder()
                .id(toLong(row.get("id")))
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .parentName((String) row.get("padre_nombre"))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        return ((Number) v).longValue();
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

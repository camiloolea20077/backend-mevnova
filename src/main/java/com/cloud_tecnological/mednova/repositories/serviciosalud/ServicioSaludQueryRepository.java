package com.cloud_tecnological.mednova.repositories.serviciosalud;

import com.cloud_tecnological.mednova.dto.serviciosalud.ServicioSaludResponseDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.ServicioSaludTableDto;
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
public class ServicioSaludQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ServicioSaludQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ServicioSaludResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT ss.id,
                       ss.empresa_id,
                       ss.codigo_interno,
                       ss.codigo_cups,
                       ss.nombre,
                       ss.descripcion,
                       ss.categoria_servicio_salud_id,
                       cs.nombre  AS categoria_nombre,
                       ss.centro_costo_id,
                       cc.nombre  AS centro_costo_nombre,
                       ss.unidad_medida,
                       ss.requiere_autorizacion,
                       ss.requiere_diagnostico,
                       ss.activo,
                       ss.created_at
                FROM servicio_salud ss
                INNER JOIN categoria_servicio_salud cs ON cs.id = ss.categoria_servicio_salud_id
                LEFT  JOIN centro_costo cc             ON cc.id = ss.centro_costo_id AND cc.deleted_at IS NULL
                WHERE ss.id = :id
                  AND ss.empresa_id = :empresa_id
                  AND ss.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public boolean existsByInternalCode(String codigo_interno, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM servicio_salud
                WHERE codigo_interno = :codigo_interno
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("codigo_interno", codigo_interno)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<ServicioSaludTableDto> listServicios(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT ss.id,
                       ss.codigo_interno,
                       ss.codigo_cups,
                       ss.nombre,
                       cs.nombre  AS categoria_nombre,
                       cc.nombre  AS centro_costo_nombre,
                       ss.requiere_autorizacion,
                       ss.activo,
                       COUNT(*) OVER() AS total_rows
                FROM servicio_salud ss
                INNER JOIN categoria_servicio_salud cs ON cs.id = ss.categoria_servicio_salud_id
                LEFT  JOIN centro_costo cc             ON cc.id = ss.centro_costo_id AND cc.deleted_at IS NULL
                WHERE ss.empresa_id = :empresa_id
                  AND ss.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        unaccent(UPPER(ss.nombre)) LIKE unaccent(UPPER(:search))
                        OR UPPER(ss.codigo_interno) LIKE UPPER(:search)
                        OR UPPER(ss.codigo_cups) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "ss.nombre";
        String order = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<ServicioSaludTableDto> dtos = result.stream().map(this::mapToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private ServicioSaludResponseDto mapToResponseDto(Map<String, Object> row) {
        return ServicioSaludResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .internalCode((String) row.get("codigo_interno"))
                .cupsCode((String) row.get("codigo_cups"))
                .name((String) row.get("nombre"))
                .description((String) row.get("descripcion"))
                .healthServiceCategoryId(toLong(row.get("categoria_servicio_salud_id")))
                .healthServiceCategoryName((String) row.get("categoria_nombre"))
                .costCenterId(toLong(row.get("centro_costo_id")))
                .costCenterName((String) row.get("centro_costo_nombre"))
                .measureUnit((String) row.get("unidad_medida"))
                .requiresAuthorization((Boolean) row.get("requiere_autorizacion"))
                .requiresDiagnosis((Boolean) row.get("requiere_diagnostico"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private ServicioSaludTableDto mapToTableDto(Map<String, Object> row) {
        return ServicioSaludTableDto.builder()
                .id(toLong(row.get("id")))
                .internalCode((String) row.get("codigo_interno"))
                .cupsCode((String) row.get("codigo_cups"))
                .name((String) row.get("nombre"))
                .healthServiceCategoryName((String) row.get("categoria_nombre"))
                .costCenterName((String) row.get("centro_costo_nombre"))
                .requiresAuthorization((Boolean) row.get("requiere_autorizacion"))
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

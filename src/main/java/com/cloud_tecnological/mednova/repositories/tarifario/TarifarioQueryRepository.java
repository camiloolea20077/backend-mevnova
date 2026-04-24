package com.cloud_tecnological.mednova.repositories.tarifario;

import com.cloud_tecnological.mednova.dto.tarifario.DetalleTarifarioResponseDto;
import com.cloud_tecnological.mednova.dto.tarifario.TarifarioResponseDto;
import com.cloud_tecnological.mednova.dto.tarifario.TarifarioTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TarifarioQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TarifarioQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<TarifarioResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT t.id, t.empresa_id, t.codigo, t.nombre, t.descripcion,
                       t.fecha_vigencia_desde, t.fecha_vigencia_hasta, t.activo, t.created_at
                FROM tarifario t
                WHERE t.id = :id
                  AND t.empresa_id = :empresa_id
                  AND t.deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("id", id).addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public boolean existsByCode(String codigo, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM tarifario
                WHERE codigo = :codigo AND empresa_id = :empresa_id
                  AND deleted_at IS NULL AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("codigo", codigo).addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<TarifarioTableDto> listTarifarios(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT t.id, t.codigo, t.nombre, t.fecha_vigencia_desde, t.fecha_vigencia_hasta,
                       t.activo, COUNT(*) OVER() AS total_rows
                FROM tarifario t
                WHERE t.empresa_id = :empresa_id AND t.deleted_at IS NULL
                """);
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (unaccent(UPPER(t.nombre)) LIKE unaccent(UPPER(:search)) OR UPPER(t.codigo) LIKE UPPER(:search))");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "t.nombre";
        String order = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows).addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<TarifarioTableDto> dtos = result.stream().map(this::mapToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();
        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ---------- Detalle tarifario ----------

    public Optional<DetalleTarifarioResponseDto> findDetalleById(Long id, Long empresa_id) {
        String sql = """
                SELECT dt.id, dt.tarifario_id, dt.servicio_salud_id,
                       ss.codigo_interno AS servicio_codigo, ss.nombre AS servicio_nombre,
                       dt.valor, dt.observaciones, dt.activo, dt.created_at
                FROM detalle_tarifario dt
                INNER JOIN servicio_salud ss ON ss.id = dt.servicio_salud_id AND ss.deleted_at IS NULL
                WHERE dt.id = :id AND dt.empresa_id = :empresa_id AND dt.activo = true
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("id", id).addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToDetalleDto(rows.get(0)));
    }

    public List<DetalleTarifarioResponseDto> listDetalles(Long tarifario_id, Long empresa_id) {
        String sql = """
                SELECT dt.id, dt.tarifario_id, dt.servicio_salud_id,
                       ss.codigo_interno AS servicio_codigo, ss.nombre AS servicio_nombre,
                       dt.valor, dt.observaciones, dt.activo, dt.created_at
                FROM detalle_tarifario dt
                INNER JOIN servicio_salud ss ON ss.id = dt.servicio_salud_id AND ss.deleted_at IS NULL
                WHERE dt.tarifario_id = :tarifario_id AND dt.empresa_id = :empresa_id AND dt.activo = true
                ORDER BY ss.nombre
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("tarifario_id", tarifario_id).addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        return rows.stream().map(this::mapToDetalleDto).toList();
    }

    public boolean existsDetalleByServicio(Long tarifario_id, Long servicio_salud_id, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM detalle_tarifario
                WHERE tarifario_id = :tarifario_id AND servicio_salud_id = :servicio_salud_id
                  AND empresa_id = :empresa_id AND activo = true AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("tarifario_id", tarifario_id).addValue("servicio_salud_id", servicio_salud_id)
                .addValue("empresa_id", empresa_id).addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    private TarifarioResponseDto mapToResponseDto(Map<String, Object> row) {
        return TarifarioResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .description((String) row.get("descripcion"))
                .validFrom(toLocalDate(row.get("fecha_vigencia_desde")))
                .validUntil(toLocalDate(row.get("fecha_vigencia_hasta")))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private TarifarioTableDto mapToTableDto(Map<String, Object> row) {
        return TarifarioTableDto.builder()
                .id(toLong(row.get("id")))
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .validFrom(toLocalDate(row.get("fecha_vigencia_desde")))
                .validUntil(toLocalDate(row.get("fecha_vigencia_hasta")))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private DetalleTarifarioResponseDto mapToDetalleDto(Map<String, Object> row) {
        return DetalleTarifarioResponseDto.builder()
                .id(toLong(row.get("id")))
                .tarifarioId(toLong(row.get("tarifario_id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .value(row.get("valor") != null ? new BigDecimal(row.get("valor").toString()) : null)
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private Long toLong(Object v) { return v == null ? null : ((Number) v).longValue(); }

    private LocalDate toLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        if (v instanceof Date d) return d.toLocalDate();
        return null;
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

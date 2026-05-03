package com.cloud_tecnological.mednova.repositories.detalleglosa;

import com.cloud_tecnological.mednova.dto.detalleglosa.DetalleGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.DetalleGlosaTableDto;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DetalleGlosaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DetalleGlosaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-063: detalle de glosa ──────────────────────────────────────

    public Optional<Map<String, Object>> findGlosaSummary(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT g.id,
                       g.empresa_id,
                       g.factura_id,
                       g.estado_glosa,
                       g.valor_total_glosado
                FROM glosa g
                WHERE g.id = :id
                  AND g.empresa_id = :empresa_id
                  AND g.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", glosaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Optional<Map<String, Object>> findDetalleFacturaSummary(Long detalleFacturaId, Long facturaId, Long empresa_id) {
        String sql = """
                SELECT df.id,
                       df.factura_id,
                       df.servicio_salud_id,
                       df.total
                FROM detalle_factura df
                WHERE df.id = :id
                  AND df.factura_id = :factura_id
                  AND df.empresa_id = :empresa_id
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", detalleFacturaId)
                .addValue("factura_id", facturaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public boolean existsActiveMotivoGlosa(Long motivoId) {
        String sql = """
                SELECT COUNT(*)
                FROM motivo_glosa
                WHERE id = :id
                  AND activo = true
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", motivoId), Long.class);
        return count != null && count > 0;
    }

    public BigDecimal sumGlosadoByDetalleFactura(Long detalleFacturaId, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COALESCE(SUM(valor_glosado), 0)
                FROM detalle_glosa
                WHERE detalle_factura_id = :detalle_factura_id
                  AND empresa_id         = :empresa_id
                  AND deleted_at IS NULL
                  AND id <> :exclude_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("detalle_factura_id", detalleFacturaId)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1L : excludeId);
        return jdbc.queryForObject(sql, params, BigDecimal.class);
    }

    public BigDecimal sumDetallesByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT COALESCE(SUM(valor_glosado), 0)
                FROM detalle_glosa
                WHERE glosa_id   = :glosa_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("glosa_id", glosaId)
                .addValue("empresa_id", empresa_id);
        return jdbc.queryForObject(sql, params, BigDecimal.class);
    }

    public Long countDetallesByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM detalle_glosa
                WHERE glosa_id   = :glosa_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("glosa_id", glosaId)
                .addValue("empresa_id", empresa_id);
        return jdbc.queryForObject(sql, params, Long.class);
    }

    public Optional<DetalleGlosaResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT dg.id,
                       dg.glosa_id,
                       dg.detalle_factura_id,
                       ss.nombre  AS servicio_nombre,
                       df.total   AS detalle_factura_total,
                       dg.motivo_glosa_id,
                       mg.codigo  AS motivo_codigo,
                       mg.nombre  AS motivo_nombre,
                       mg.grupo   AS motivo_grupo,
                       dg.valor_glosado,
                       dg.observacion_pagador,
                       dg.activo,
                       dg.created_at
                FROM detalle_glosa dg
                INNER JOIN detalle_factura df ON df.id = dg.detalle_factura_id
                LEFT  JOIN servicio_salud ss  ON ss.id = df.servicio_salud_id
                LEFT  JOIN motivo_glosa mg    ON mg.id = dg.motivo_glosa_id
                WHERE dg.id = :id
                  AND dg.empresa_id = :empresa_id
                  AND dg.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public List<DetalleGlosaTableDto> listByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT dg.id,
                       dg.detalle_factura_id,
                       ss.nombre AS servicio_nombre,
                       df.total  AS detalle_factura_total,
                       mg.codigo AS motivo_codigo,
                       mg.nombre AS motivo_nombre,
                       dg.valor_glosado,
                       dg.activo
                FROM detalle_glosa dg
                INNER JOIN detalle_factura df ON df.id = dg.detalle_factura_id
                LEFT  JOIN servicio_salud ss  ON ss.id = df.servicio_salud_id
                LEFT  JOIN motivo_glosa mg    ON mg.id = dg.motivo_glosa_id
                WHERE dg.glosa_id   = :glosa_id
                  AND dg.empresa_id = :empresa_id
                  AND dg.deleted_at IS NULL
                ORDER BY dg.created_at ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("glosa_id", glosaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream().map(this::mapRowToTableDto).toList();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private DetalleGlosaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return DetalleGlosaResponseDto.builder()
                .id(toLong(row.get("id")))
                .glossId(toLong(row.get("glosa_id")))
                .invoiceItemId(toLong(row.get("detalle_factura_id")))
                .serviceName((String) row.get("servicio_nombre"))
                .invoiceItemTotal(toBigDecimal(row.get("detalle_factura_total")))
                .glossReasonId(toLong(row.get("motivo_glosa_id")))
                .glossReasonCode((String) row.get("motivo_codigo"))
                .glossReasonName((String) row.get("motivo_nombre"))
                .glossReasonGroup((String) row.get("motivo_grupo"))
                .glossedValue(toBigDecimal(row.get("valor_glosado")))
                .payerObservation((String) row.get("observacion_pagador"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private DetalleGlosaTableDto mapRowToTableDto(Map<String, Object> row) {
        return DetalleGlosaTableDto.builder()
                .id(toLong(row.get("id")))
                .invoiceItemId(toLong(row.get("detalle_factura_id")))
                .serviceName((String) row.get("servicio_nombre"))
                .invoiceItemTotal(toBigDecimal(row.get("detalle_factura_total")))
                .glossReasonCode((String) row.get("motivo_codigo"))
                .glossReasonName((String) row.get("motivo_nombre"))
                .glossedValue(toBigDecimal(row.get("valor_glosado")))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

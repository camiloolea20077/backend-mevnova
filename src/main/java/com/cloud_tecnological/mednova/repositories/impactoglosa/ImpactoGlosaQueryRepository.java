package com.cloud_tecnological.mednova.repositories.impactoglosa;

import com.cloud_tecnological.mednova.dto.impactoglosa.ImpactoGlosaCarteraDto;
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
public class ImpactoGlosaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ImpactoGlosaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Long> findFacturaIdByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT factura_id
                FROM glosa
                WHERE id = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", glosaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(((Number) rows.get(0).get("factura_id")).longValue());
    }

    public Long findEstadoFacturaIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_factura WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public boolean allGlosasOfFacturaClosed(Long facturaId, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM glosa
                WHERE factura_id = :factura_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND estado_glosa NOT IN ('CERRADA','ANULADA')
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("factura_id", facturaId)
                .addValue("empresa_id", empresa_id);
        Long pendientes = jdbc.queryForObject(sql, params, Long.class);
        return pendientes != null && pendientes == 0L;
    }

    public Optional<ImpactoGlosaCarteraDto> findImpactoByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT cg.glosa_id,
                       cg.id              AS concertation_id,
                       g.factura_id,
                       cxc.id             AS cxc_id,
                       m.id               AS movement_id,
                       m.tipo_movimiento,
                       m.valor,
                       m.saldo_resultante,
                       m.fecha_movimiento
                FROM movimiento_cuenta_por_cobrar m
                INNER JOIN cuenta_por_cobrar cxc ON cxc.id = m.cuenta_por_cobrar_id
                INNER JOIN glosa g               ON g.factura_id = cxc.factura_id
                INNER JOIN concertacion_glosa cg ON cg.glosa_id = g.id AND cg.deleted_at IS NULL
                WHERE g.id          = :glosa_id
                  AND m.empresa_id  = :empresa_id
                  AND m.tipo_movimiento = 'GLOSA_ACEPTADA'
                  AND m.referencia  = CONCAT('CONCERTACION_GLOSA:', cg.id)
                ORDER BY m.fecha_movimiento DESC
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("glosa_id", glosaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        Map<String, Object> row = rows.get(0);

        BigDecimal valor = toBigDecimal(row.get("valor"));
        BigDecimal saldoPost = toBigDecimal(row.get("saldo_resultante"));
        BigDecimal saldoPrev = saldoPost == null || valor == null ? null : saldoPost.add(valor);

        return Optional.of(ImpactoGlosaCarteraDto.builder()
                .glossId(toLong(row.get("glosa_id")))
                .concertationId(toLong(row.get("concertation_id")))
                .invoiceId(toLong(row.get("factura_id")))
                .accountReceivableId(toLong(row.get("cxc_id")))
                .movementId(toLong(row.get("movement_id")))
                .movementType((String) row.get("tipo_movimiento"))
                .movementValue(valor)
                .accountPreviousBalance(saldoPrev)
                .accountNewBalance(saldoPost)
                .movementDate(toLocalDateTime(row.get("fecha_movimiento")))
                .build());
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

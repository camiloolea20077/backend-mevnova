package com.cloud_tecnological.mednova.repositories.glosa;

import com.cloud_tecnological.mednova.dto.glosa.GlosaResponseDto;
import com.cloud_tecnological.mednova.dto.glosa.GlosaTableDto;
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
public class GlosaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public GlosaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-062: recepción de glosa ─────────────────────────────────────

    public Optional<Map<String, Object>> findFacturaSummaryById(Long facturaId, Long empresa_id) {
        String sql = """
                SELECT f.id,
                       f.empresa_id,
                       f.sede_id,
                       f.prefijo,
                       f.numero,
                       f.pagador_id,
                       f.estado_factura_id,
                       f.total_neto,
                       ef.codigo AS estado_codigo
                FROM factura f
                INNER JOIN estado_factura ef ON ef.id = f.estado_factura_id
                WHERE f.id = :id
                  AND f.empresa_id = :empresa_id
                  AND f.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", facturaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Long findEstadoFacturaIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_factura WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public Optional<Long> findRadicacionIdByFactura(Long facturaId, Long empresa_id) {
        String sql = """
                SELECT id
                FROM radicacion
                WHERE factura_id = :factura_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                ORDER BY created_at DESC
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("factura_id", facturaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty()
                ? Optional.empty()
                : Optional.of(((Number) rows.get(0).get("id")).longValue());
    }

    public BigDecimal sumGlosadoByFactura(Long facturaId, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COALESCE(SUM(valor_total_glosado), 0) AS total
                FROM glosa
                WHERE factura_id = :factura_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND estado_glosa <> 'ANULADA'
                  AND id <> :exclude_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("factura_id", facturaId)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1L : excludeId);
        BigDecimal total = jdbc.queryForObject(sql, params, BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    public Optional<GlosaResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT g.id,
                       g.factura_id,
                       f.numero       AS factura_numero,
                       f.prefijo      AS factura_prefijo,
                       f.total_neto   AS factura_total,
                       g.radicacion_id,
                       r.numero_radicado AS radicacion_numero,
                       tp.nombre_completo AS pagador_nombre,
                       g.numero_oficio_pagador,
                       g.fecha_oficio,
                       g.fecha_notificacion,
                       g.valor_total_glosado,
                       g.oficio_url,
                       g.fecha_limite_respuesta,
                       g.estado_glosa,
                       g.observaciones,
                       g.activo,
                       g.created_at
                FROM glosa g
                INNER JOIN factura f ON f.id = g.factura_id
                LEFT JOIN radicacion r ON r.id = g.radicacion_id
                LEFT JOIN pagador pag ON pag.id = f.pagador_id
                LEFT JOIN tercero tp  ON tp.id = pag.tercero_id
                WHERE g.id = :id
                  AND g.empresa_id = :empresa_id
                  AND g.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public PageImpl<GlosaTableDto> listGlosas(PageableDto<?> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT g.id,
                       CONCAT(COALESCE(f.prefijo, ''), f.numero) AS factura_numero,
                       tp.nombre_completo AS pagador_nombre,
                       g.numero_oficio_pagador,
                       g.fecha_notificacion,
                       g.fecha_limite_respuesta,
                       g.valor_total_glosado,
                       g.estado_glosa,
                       g.activo,
                       COUNT(*) OVER() AS total_rows
                FROM glosa g
                INNER JOIN factura f ON f.id = g.factura_id
                LEFT JOIN pagador pag ON pag.id = f.pagador_id
                LEFT JOIN tercero tp  ON tp.id = pag.tercero_id
                WHERE g.empresa_id = :empresa_id
                  AND g.sede_id    = :sede_id
                  AND g.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(g.numero_oficio_pagador) LIKE UPPER(:search)
                        OR UPPER(f.numero) LIKE UPPER(:search)
                        OR UPPER(tp.nombre_completo) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "g.fecha_notificacion";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<GlosaTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private GlosaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return GlosaResponseDto.builder()
                .id(toLong(row.get("id")))
                .invoiceId(toLong(row.get("factura_id")))
                .invoiceNumber((String) row.get("factura_numero"))
                .invoicePrefix((String) row.get("factura_prefijo"))
                .invoiceTotalValue(toBigDecimal(row.get("factura_total")))
                .radicationId(toLong(row.get("radicacion_id")))
                .radicationNumber((String) row.get("radicacion_numero"))
                .payerName((String) row.get("pagador_nombre"))
                .payerOfficeNumber((String) row.get("numero_oficio_pagador"))
                .officeDate(toLocalDate(row.get("fecha_oficio")))
                .notificationDate(toLocalDate(row.get("fecha_notificacion")))
                .totalGlossedValue(toBigDecimal(row.get("valor_total_glosado")))
                .officeUrl((String) row.get("oficio_url"))
                .responseDeadline(toLocalDate(row.get("fecha_limite_respuesta")))
                .status((String) row.get("estado_glosa"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private GlosaTableDto mapRowToTableDto(Map<String, Object> row) {
        return GlosaTableDto.builder()
                .id(toLong(row.get("id")))
                .invoiceNumber((String) row.get("factura_numero"))
                .payerName((String) row.get("pagador_nombre"))
                .payerOfficeNumber((String) row.get("numero_oficio_pagador"))
                .notificationDate(toLocalDate(row.get("fecha_notificacion")))
                .responseDeadline(toLocalDate(row.get("fecha_limite_respuesta")))
                .totalGlossedValue(toBigDecimal(row.get("valor_total_glosado")))
                .status((String) row.get("estado_glosa"))
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

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

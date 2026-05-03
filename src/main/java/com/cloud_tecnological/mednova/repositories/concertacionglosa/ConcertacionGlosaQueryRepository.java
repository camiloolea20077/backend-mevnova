package com.cloud_tecnological.mednova.repositories.concertacionglosa;

import com.cloud_tecnological.mednova.dto.concertacionglosa.ConcertacionGlosaResponseDto;
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
public class ConcertacionGlosaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ConcertacionGlosaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-065: conciliación y cierre de glosa ────────────────────────

    public Optional<Map<String, Object>> findGlosaSummary(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT g.id,
                       g.empresa_id,
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

    public boolean existsByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM concertacion_glosa
                WHERE glosa_id   = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", glosaId)
                .addValue("empresa_id", empresa_id);
        Long total = jdbc.queryForObject(sql, params, Long.class);
        return total != null && total > 0;
    }

    public Optional<ConcertacionGlosaResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT cg.id,
                       cg.glosa_id,
                       g.estado_glosa,
                       cg.fecha_concertacion,
                       cg.valor_glosa_inicial,
                       cg.valor_aceptado_institucion,
                       cg.valor_aceptado_pagador,
                       cg.valor_recuperado,
                       cg.acta_url,
                       cg.observaciones,
                       cg.activo,
                       cg.created_at
                FROM concertacion_glosa cg
                INNER JOIN glosa g ON g.id = cg.glosa_id
                WHERE cg.id = :id
                  AND cg.empresa_id = :empresa_id
                  AND cg.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public Optional<ConcertacionGlosaResponseDto> findByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT cg.id
                FROM concertacion_glosa cg
                WHERE cg.glosa_id   = :id
                  AND cg.empresa_id = :empresa_id
                  AND cg.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", glosaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        Long id = ((Number) rows.get(0).get("id")).longValue();
        return findActiveById(id, empresa_id);
    }

    public void updateEstadoGlosa(Long glosaId, Long empresa_id, String nuevoEstado, Long usuarioId) {
        String sql = """
                UPDATE glosa
                SET estado_glosa         = :estado,
                    updated_at           = NOW(),
                    usuario_modificacion = :usuario
                WHERE id = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("estado", nuevoEstado)
                .addValue("usuario", usuarioId)
                .addValue("id", glosaId)
                .addValue("empresa_id", empresa_id);
        jdbc.update(sql, params);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private ConcertacionGlosaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return ConcertacionGlosaResponseDto.builder()
                .id(toLong(row.get("id")))
                .glossId(toLong(row.get("glosa_id")))
                .glossStatus((String) row.get("estado_glosa"))
                .concertationDate(toLocalDate(row.get("fecha_concertacion")))
                .initialGlossValue(toBigDecimal(row.get("valor_glosa_inicial")))
                .institutionAcceptedValue(toBigDecimal(row.get("valor_aceptado_institucion")))
                .payerAcceptedValue(toBigDecimal(row.get("valor_aceptado_pagador")))
                .recoveredValue(toBigDecimal(row.get("valor_recuperado")))
                .actaUrl((String) row.get("acta_url"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
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

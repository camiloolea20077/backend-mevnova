package com.cloud_tecnological.mednova.repositories.respuestaglosa;

import com.cloud_tecnological.mednova.dto.respuestaglosa.RespuestaGlosaResponseDto;
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
public class RespuestaGlosaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RespuestaGlosaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-064: respuesta a glosa por ítem ────────────────────────────

    /**
     * Devuelve los datos del detalle_glosa junto con la glosa cabecera para validar
     * empresa, estado y obtener valor_glosado / fecha_limite_respuesta.
     */
    public Optional<Map<String, Object>> findDetalleConGlosa(Long detalleGlosaId, Long empresa_id) {
        String sql = """
                SELECT dg.id              AS detalle_id,
                       dg.glosa_id,
                       dg.empresa_id,
                       dg.valor_glosado,
                       g.estado_glosa,
                       g.fecha_limite_respuesta
                FROM detalle_glosa dg
                INNER JOIN glosa g ON g.id = dg.glosa_id AND g.deleted_at IS NULL
                WHERE dg.id = :id
                  AND dg.empresa_id = :empresa_id
                  AND dg.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", detalleGlosaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public boolean existsByDetalleGlosa(Long detalleGlosaId, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM respuesta_glosa
                WHERE detalle_glosa_id = :id
                  AND empresa_id       = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", detalleGlosaId)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsProfesionalByIdAndEmpresa(Long profesionalId, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM profesional_salud
                WHERE id = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", profesionalId)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public long countDetallesByGlosa(Long glosaId, Long empresa_id) {
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
        Long total = jdbc.queryForObject(sql, params, Long.class);
        return total == null ? 0 : total;
    }

    public long countRespuestasByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM respuesta_glosa
                WHERE glosa_id   = :glosa_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("glosa_id", glosaId)
                .addValue("empresa_id", empresa_id);
        Long total = jdbc.queryForObject(sql, params, Long.class);
        return total == null ? 0 : total;
    }

    public Optional<RespuestaGlosaResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT rg.id,
                       rg.glosa_id,
                       rg.detalle_glosa_id,
                       dg.valor_glosado,
                       rg.tipo_respuesta,
                       rg.valor_aceptado,
                       rg.argumentacion,
                       rg.soporte_url,
                       rg.profesional_respuesta_id,
                       t.nombre_completo AS profesional_nombre,
                       rg.fecha_respuesta,
                       rg.activo,
                       rg.created_at,
                       g.fecha_limite_respuesta
                FROM respuesta_glosa rg
                INNER JOIN detalle_glosa dg ON dg.id = rg.detalle_glosa_id
                INNER JOIN glosa g          ON g.id = rg.glosa_id
                LEFT  JOIN profesional_salud p ON p.id = rg.profesional_respuesta_id
                LEFT  JOIN tercero t           ON t.id = p.tercero_id
                WHERE rg.id = :id
                  AND rg.empresa_id = :empresa_id
                  AND rg.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public Optional<RespuestaGlosaResponseDto> findByDetalleGlosa(Long detalleGlosaId, Long empresa_id) {
        String sql = """
                SELECT rg.id
                FROM respuesta_glosa rg
                WHERE rg.detalle_glosa_id = :id
                  AND rg.empresa_id       = :empresa_id
                  AND rg.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", detalleGlosaId)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        Long id = ((Number) rows.get(0).get("id")).longValue();
        return findActiveById(id, empresa_id);
    }

    public List<RespuestaGlosaResponseDto> listByGlosa(Long glosaId, Long empresa_id) {
        String sql = """
                SELECT rg.id,
                       rg.glosa_id,
                       rg.detalle_glosa_id,
                       dg.valor_glosado,
                       rg.tipo_respuesta,
                       rg.valor_aceptado,
                       rg.argumentacion,
                       rg.soporte_url,
                       rg.profesional_respuesta_id,
                       t.nombre_completo AS profesional_nombre,
                       rg.fecha_respuesta,
                       rg.activo,
                       rg.created_at,
                       g.fecha_limite_respuesta
                FROM respuesta_glosa rg
                INNER JOIN detalle_glosa dg ON dg.id = rg.detalle_glosa_id
                INNER JOIN glosa g          ON g.id = rg.glosa_id
                LEFT  JOIN profesional_salud p ON p.id = rg.profesional_respuesta_id
                LEFT  JOIN tercero t           ON t.id = p.tercero_id
                WHERE rg.glosa_id   = :glosa_id
                  AND rg.empresa_id = :empresa_id
                  AND rg.deleted_at IS NULL
                ORDER BY rg.created_at ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("glosa_id", glosaId)
                .addValue("empresa_id", empresa_id);
        return jdbc.query(sql, params, new ColumnMapRowMapper())
                .stream().map(this::mapRowToResponseDto).toList();
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

    private RespuestaGlosaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        LocalDate fechaLimite = toLocalDate(row.get("fecha_limite_respuesta"));
        LocalDateTime fechaResp = toLocalDateTime(row.get("fecha_respuesta"));
        boolean overdue = fechaLimite != null && fechaResp != null
                && fechaResp.toLocalDate().isAfter(fechaLimite);

        return RespuestaGlosaResponseDto.builder()
                .id(toLong(row.get("id")))
                .glossId(toLong(row.get("glosa_id")))
                .glossDetailId(toLong(row.get("detalle_glosa_id")))
                .glossedValue(toBigDecimal(row.get("valor_glosado")))
                .responseType((String) row.get("tipo_respuesta"))
                .acceptedValue(toBigDecimal(row.get("valor_aceptado")))
                .argumentation((String) row.get("argumentacion"))
                .supportUrl((String) row.get("soporte_url"))
                .professionalId(toLong(row.get("profesional_respuesta_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .responseDate(fechaResp)
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .overdue(overdue)
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

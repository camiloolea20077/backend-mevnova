package com.cloud_tecnological.mednova.repositories.sisbenpaciente;

import com.cloud_tecnological.mednova.dto.sisbenpaciente.SisbenPacienteResponseDto;
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
public class SisbenPacienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SisbenPacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<SisbenPacienteResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT sp.id, sp.empresa_id, sp.paciente_id, sp.grupo_sisben_id,
                       gs.nombre AS grupo_nombre,
                       sp.puntaje, sp.ficha_sisben, sp.fecha_encuesta,
                       sp.fecha_vigencia_desde, sp.fecha_vigencia_hasta,
                       sp.vigente, sp.observaciones, sp.activo, sp.created_at
                FROM sisben_paciente sp
                LEFT JOIN grupo_sisben gs ON gs.id = sp.grupo_sisben_id
                WHERE sp.id = :id AND sp.empresa_id = :empresa_id AND sp.activo = true
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource().addValue("id", id).addValue("empresa_id", empresa_id),
                new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToDto(rows.get(0)));
    }

    public List<SisbenPacienteResponseDto> listByPaciente(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT sp.id, sp.empresa_id, sp.paciente_id, sp.grupo_sisben_id,
                       gs.nombre AS grupo_nombre,
                       sp.puntaje, sp.ficha_sisben, sp.fecha_encuesta,
                       sp.fecha_vigencia_desde, sp.fecha_vigencia_hasta,
                       sp.vigente, sp.observaciones, sp.activo, sp.created_at
                FROM sisben_paciente sp
                LEFT JOIN grupo_sisben gs ON gs.id = sp.grupo_sisben_id
                WHERE sp.paciente_id = :paciente_id AND sp.empresa_id = :empresa_id AND sp.activo = true
                ORDER BY sp.fecha_vigencia_desde DESC
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource().addValue("paciente_id", paciente_id).addValue("empresa_id", empresa_id),
                new ColumnMapRowMapper());
        return rows.stream().map(this::mapToDto).toList();
    }

    public void deactivateVigentesForPaciente(Long paciente_id, Long empresa_id, Long usuario_id) {
        String sql = """
                UPDATE sisben_paciente SET vigente = false, usuario_modificacion = :usuario_id
                WHERE paciente_id = :paciente_id AND empresa_id = :empresa_id AND vigente = true AND activo = true
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("paciente_id", paciente_id)
                .addValue("empresa_id", empresa_id)
                .addValue("usuario_id", usuario_id));
    }

    private SisbenPacienteResponseDto mapToDto(Map<String, Object> row) {
        return SisbenPacienteResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .patientId(toLong(row.get("paciente_id")))
                .sisbenGroupId(toLong(row.get("grupo_sisben_id")))
                .sisbenGroupName((String) row.get("grupo_nombre"))
                .score(toBigDecimal(row.get("puntaje")))
                .sisbenCard((String) row.get("ficha_sisben"))
                .surveyDate(toLocalDate(row.get("fecha_encuesta")))
                .validFrom(toLocalDate(row.get("fecha_vigencia_desde")))
                .validUntil(toLocalDate(row.get("fecha_vigencia_hasta")))
                .current((Boolean) row.get("vigente"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private Long toLong(Object v) { return v == null ? null : ((Number) v).longValue(); }
    private BigDecimal toBigDecimal(Object v) { return v == null ? null : new BigDecimal(v.toString()); }

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

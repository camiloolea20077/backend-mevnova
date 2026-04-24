package com.cloud_tecnological.mednova.repositories.seguridadsocialpaciente;

import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.SeguridadSocialPacienteResponseDto;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SeguridadSocialPacienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SeguridadSocialPacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<SeguridadSocialPacienteResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT ssp.id, ssp.empresa_id, ssp.paciente_id, ssp.pagador_id,
                       t.nombre_completo AS pagador_nombre,
                       ssp.regimen_id, cat_reg.nombre AS regimen_nombre,
                       ssp.categoria_afiliacion_id, ssp.tipo_afiliacion_id,
                       ssp.numero_afiliacion, ssp.tercero_cotizante_id,
                       tc.nombre_completo AS cotizante_nombre,
                       ssp.fecha_afiliacion, ssp.fecha_vigencia_desde, ssp.fecha_vigencia_hasta,
                       ssp.vigente, ssp.observaciones, ssp.activo, ssp.created_at
                FROM seguridad_social_paciente ssp
                INNER JOIN pagador p ON p.id = ssp.pagador_id AND p.deleted_at IS NULL
                INNER JOIN tercero t ON t.id = p.tercero_id AND t.deleted_at IS NULL
                LEFT JOIN regimen cat_reg ON cat_reg.id = ssp.regimen_id
                LEFT JOIN tercero tc ON tc.id = ssp.tercero_cotizante_id AND tc.deleted_at IS NULL
                WHERE ssp.id = :id AND ssp.empresa_id = :empresa_id AND ssp.activo = true
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource().addValue("id", id).addValue("empresa_id", empresa_id),
                new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToDto(rows.get(0)));
    }

    public List<SeguridadSocialPacienteResponseDto> listByPaciente(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT ssp.id, ssp.empresa_id, ssp.paciente_id, ssp.pagador_id,
                       t.nombre_completo AS pagador_nombre,
                       ssp.regimen_id, cat_reg.nombre AS regimen_nombre,
                       ssp.categoria_afiliacion_id, ssp.tipo_afiliacion_id,
                       ssp.numero_afiliacion, ssp.tercero_cotizante_id,
                       tc.nombre_completo AS cotizante_nombre,
                       ssp.fecha_afiliacion, ssp.fecha_vigencia_desde, ssp.fecha_vigencia_hasta,
                       ssp.vigente, ssp.observaciones, ssp.activo, ssp.created_at
                FROM seguridad_social_paciente ssp
                INNER JOIN pagador p ON p.id = ssp.pagador_id AND p.deleted_at IS NULL
                INNER JOIN tercero t ON t.id = p.tercero_id AND t.deleted_at IS NULL
                LEFT JOIN regimen cat_reg ON cat_reg.id = ssp.regimen_id
                LEFT JOIN tercero tc ON tc.id = ssp.tercero_cotizante_id AND tc.deleted_at IS NULL
                WHERE ssp.paciente_id = :paciente_id AND ssp.empresa_id = :empresa_id AND ssp.activo = true
                ORDER BY ssp.fecha_vigencia_desde DESC
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource().addValue("paciente_id", paciente_id).addValue("empresa_id", empresa_id),
                new ColumnMapRowMapper());
        return rows.stream().map(this::mapToDto).toList();
    }

    public void deactivateVigentesForPaciente(Long paciente_id, Long empresa_id, Long usuario_id) {
        String sql = """
                UPDATE seguridad_social_paciente SET vigente = false, usuario_modificacion = :usuario_id
                WHERE paciente_id = :paciente_id AND empresa_id = :empresa_id AND vigente = true AND activo = true
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("paciente_id", paciente_id)
                .addValue("empresa_id", empresa_id)
                .addValue("usuario_id", usuario_id));
    }

    private SeguridadSocialPacienteResponseDto mapToDto(Map<String, Object> row) {
        return SeguridadSocialPacienteResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .patientId(toLong(row.get("paciente_id")))
                .payerId(toLong(row.get("pagador_id")))
                .payerName((String) row.get("pagador_nombre"))
                .regimenId(toLong(row.get("regimen_id")))
                .regimenName((String) row.get("regimen_nombre"))
                .affiliationCategoryId(toLong(row.get("categoria_afiliacion_id")))
                .affiliationTypeId(toLong(row.get("tipo_afiliacion_id")))
                .affiliationNumber((String) row.get("numero_afiliacion"))
                .cotizanteThirdPartyId(toLong(row.get("tercero_cotizante_id")))
                .cotizanteFullName((String) row.get("cotizante_nombre"))
                .affiliationDate(toLocalDate(row.get("fecha_afiliacion")))
                .validFrom(toLocalDate(row.get("fecha_vigencia_desde")))
                .validUntil(toLocalDate(row.get("fecha_vigencia_hasta")))
                .current((Boolean) row.get("vigente"))
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

package com.cloud_tecnological.mednova.repositories.contratopaciente;

import com.cloud_tecnological.mednova.dto.contratopaciente.ContratoPacienteResponseDto;
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
public class ContratoPacienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ContratoPacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ContratoPacienteResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT cp.id, cp.empresa_id, cp.paciente_id, cp.contrato_id,
                       c.numero AS contrato_numero,
                       t.nombre_completo AS pagador_nombre,
                       cp.numero_poliza, cp.fecha_vigencia_desde, cp.fecha_vigencia_hasta,
                       cp.vigente, cp.observaciones, cp.activo, cp.created_at
                FROM contrato_paciente cp
                INNER JOIN contrato c ON c.id = cp.contrato_id AND c.deleted_at IS NULL
                INNER JOIN pagador p ON p.id = c.pagador_id AND p.deleted_at IS NULL
                INNER JOIN tercero t ON t.id = p.tercero_id AND t.deleted_at IS NULL
                WHERE cp.id = :id AND cp.empresa_id = :empresa_id AND cp.activo = true
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource().addValue("id", id).addValue("empresa_id", empresa_id),
                new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToDto(rows.get(0)));
    }

    public List<ContratoPacienteResponseDto> listByPaciente(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT cp.id, cp.empresa_id, cp.paciente_id, cp.contrato_id,
                       c.numero AS contrato_numero,
                       t.nombre_completo AS pagador_nombre,
                       cp.numero_poliza, cp.fecha_vigencia_desde, cp.fecha_vigencia_hasta,
                       cp.vigente, cp.observaciones, cp.activo, cp.created_at
                FROM contrato_paciente cp
                INNER JOIN contrato c ON c.id = cp.contrato_id AND c.deleted_at IS NULL
                INNER JOIN pagador p ON p.id = c.pagador_id AND p.deleted_at IS NULL
                INNER JOIN tercero t ON t.id = p.tercero_id AND t.deleted_at IS NULL
                WHERE cp.paciente_id = :paciente_id AND cp.empresa_id = :empresa_id AND cp.activo = true
                ORDER BY cp.fecha_vigencia_desde DESC
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource().addValue("paciente_id", paciente_id).addValue("empresa_id", empresa_id),
                new ColumnMapRowMapper());
        return rows.stream().map(this::mapToDto).toList();
    }

    private ContratoPacienteResponseDto mapToDto(Map<String, Object> row) {
        return ContratoPacienteResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .patientId(toLong(row.get("paciente_id")))
                .contractId(toLong(row.get("contrato_id")))
                .contractNumber((String) row.get("contrato_numero"))
                .payerName((String) row.get("pagador_nombre"))
                .policyNumber((String) row.get("numero_poliza"))
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

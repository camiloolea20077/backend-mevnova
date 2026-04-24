package com.cloud_tecnological.mednova.repositories.serviciocontrato;

import com.cloud_tecnological.mednova.dto.serviciocontrato.ServicioContratoResponseDto;
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
public class ServicioContratoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ServicioContratoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ServicioContratoResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT sc.id, sc.empresa_id, sc.contrato_id, sc.servicio_salud_id,
                       ss.codigo_interno AS servicio_codigo, ss.nombre AS servicio_nombre,
                       sc.requiere_autorizacion, sc.cantidad_maxima, sc.observaciones,
                       sc.activo, sc.created_at
                FROM servicio_contrato sc
                INNER JOIN servicio_salud ss ON ss.id = sc.servicio_salud_id AND ss.deleted_at IS NULL
                WHERE sc.id = :id AND sc.empresa_id = :empresa_id AND sc.activo = true
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource().addValue("id", id).addValue("empresa_id", empresa_id),
                new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToDto(rows.get(0)));
    }

    public List<ServicioContratoResponseDto> listByContrato(Long contrato_id, Long empresa_id) {
        String sql = """
                SELECT sc.id, sc.empresa_id, sc.contrato_id, sc.servicio_salud_id,
                       ss.codigo_interno AS servicio_codigo, ss.nombre AS servicio_nombre,
                       sc.requiere_autorizacion, sc.cantidad_maxima, sc.observaciones,
                       sc.activo, sc.created_at
                FROM servicio_contrato sc
                INNER JOIN servicio_salud ss ON ss.id = sc.servicio_salud_id AND ss.deleted_at IS NULL
                WHERE sc.contrato_id = :contrato_id AND sc.empresa_id = :empresa_id AND sc.activo = true
                ORDER BY ss.nombre
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource().addValue("contrato_id", contrato_id).addValue("empresa_id", empresa_id),
                new ColumnMapRowMapper());
        return rows.stream().map(this::mapToDto).toList();
    }

    public boolean existsByServicio(Long contrato_id, Long servicio_salud_id, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM servicio_contrato
                WHERE contrato_id = :contrato_id AND servicio_salud_id = :servicio_salud_id
                  AND empresa_id = :empresa_id AND activo = true AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("contrato_id", contrato_id)
                .addValue("servicio_salud_id", servicio_salud_id)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    private ServicioContratoResponseDto mapToDto(Map<String, Object> row) {
        return ServicioContratoResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .contractId(toLong(row.get("contrato_id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .requiresAuthorization((Boolean) row.get("requiere_autorizacion"))
                .maxQuantity(row.get("cantidad_maxima") == null ? null : ((Number) row.get("cantidad_maxima")).intValue())
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private Long toLong(Object v) { return v == null ? null : ((Number) v).longValue(); }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

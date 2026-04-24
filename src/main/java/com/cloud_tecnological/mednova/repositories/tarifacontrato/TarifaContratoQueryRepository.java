package com.cloud_tecnological.mednova.repositories.tarifacontrato;

import com.cloud_tecnological.mednova.dto.tarifacontrato.TarifaContratoResponseDto;
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
public class TarifaContratoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TarifaContratoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<TarifaContratoResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT tc.id, tc.empresa_id, tc.contrato_id, tc.servicio_salud_id,
                       ss.codigo_interno AS servicio_codigo, ss.nombre AS servicio_nombre,
                       tc.valor, tc.porcentaje_descuento,
                       tc.fecha_vigencia_desde, tc.fecha_vigencia_hasta,
                       tc.vigente, tc.observaciones, tc.activo, tc.created_at
                FROM tarifa_contrato tc
                INNER JOIN servicio_salud ss ON ss.id = tc.servicio_salud_id AND ss.deleted_at IS NULL
                WHERE tc.id = :id AND tc.empresa_id = :empresa_id AND tc.activo = true
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("id", id).addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToDto(rows.get(0)));
    }

    public List<TarifaContratoResponseDto> listByContrato(Long contrato_id, Long empresa_id) {
        String sql = """
                SELECT tc.id, tc.empresa_id, tc.contrato_id, tc.servicio_salud_id,
                       ss.codigo_interno AS servicio_codigo, ss.nombre AS servicio_nombre,
                       tc.valor, tc.porcentaje_descuento,
                       tc.fecha_vigencia_desde, tc.fecha_vigencia_hasta,
                       tc.vigente, tc.observaciones, tc.activo, tc.created_at
                FROM tarifa_contrato tc
                INNER JOIN servicio_salud ss ON ss.id = tc.servicio_salud_id AND ss.deleted_at IS NULL
                WHERE tc.contrato_id = :contrato_id AND tc.empresa_id = :empresa_id AND tc.activo = true
                ORDER BY ss.nombre
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("contrato_id", contrato_id).addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        return rows.stream().map(this::mapToDto).toList();
    }

    public boolean existsByServicio(Long contrato_id, Long servicio_salud_id, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM tarifa_contrato
                WHERE contrato_id = :contrato_id AND servicio_salud_id = :servicio_salud_id
                  AND empresa_id = :empresa_id AND activo = true AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("contrato_id", contrato_id).addValue("servicio_salud_id", servicio_salud_id)
                .addValue("empresa_id", empresa_id).addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    private TarifaContratoResponseDto mapToDto(Map<String, Object> row) {
        return TarifaContratoResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .contractId(toLong(row.get("contrato_id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .value(toBigDecimal(row.get("valor")))
                .discountPercentage(toBigDecimal(row.get("porcentaje_descuento")))
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

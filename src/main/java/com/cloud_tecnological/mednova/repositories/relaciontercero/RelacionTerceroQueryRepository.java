package com.cloud_tecnological.mednova.repositories.relaciontercero;

import com.cloud_tecnological.mednova.dto.relaciontercero.RelacionTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.RelacionTerceroTableDto;
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
public class RelacionTerceroQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RelacionTerceroQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<RelacionTerceroResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT rt.id,
                       rt.empresa_id,
                       rt.tercero_origen_id,
                       to_.nombre_completo AS tercero_origen_nombre,
                       rt.tercero_destino_id,
                       td_.nombre_completo AS tercero_destino_nombre,
                       rt.tipo_relacion_id,
                       tr.nombre           AS tipo_relacion_nombre,
                       rt.es_responsable,
                       rt.es_contacto_emergencia,
                       rt.observaciones,
                       rt.activo,
                       rt.created_at
                FROM relacion_tercero rt
                INNER JOIN tercero to_       ON to_.id = rt.tercero_origen_id
                INNER JOIN tercero td_       ON td_.id = rt.tercero_destino_id
                INNER JOIN tipo_relacion tr  ON tr.id  = rt.tipo_relacion_id
                WHERE rt.id = :id
                  AND rt.empresa_id = :empresa_id
                  AND rt.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public List<RelacionTerceroTableDto> listByThirdParty(Long tercero_id, Long empresa_id) {
        String sql = """
                SELECT rt.id,
                       to_.nombre_completo AS tercero_origen_nombre,
                       td_.nombre_completo AS tercero_destino_nombre,
                       tr.nombre           AS tipo_relacion_nombre,
                       rt.es_responsable,
                       rt.es_contacto_emergencia,
                       rt.activo
                FROM relacion_tercero rt
                INNER JOIN tercero to_       ON to_.id = rt.tercero_origen_id
                INNER JOIN tercero td_       ON td_.id = rt.tercero_destino_id
                INNER JOIN tipo_relacion tr  ON tr.id  = rt.tipo_relacion_id
                WHERE (rt.tercero_origen_id = :tercero_id OR rt.tercero_destino_id = :tercero_id)
                  AND rt.empresa_id = :empresa_id
                  AND rt.deleted_at IS NULL
                ORDER BY tr.nombre ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("empresa_id", empresa_id);
        return jdbc.query(sql, params, new ColumnMapRowMapper()).stream()
                .map(this::mapToTableDto).toList();
    }

    private RelacionTerceroResponseDto mapToResponseDto(Map<String, Object> row) {
        return RelacionTerceroResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .sourceThirdPartyId(toLong(row.get("tercero_origen_id")))
                .sourceFullName((String) row.get("tercero_origen_nombre"))
                .destinationThirdPartyId(toLong(row.get("tercero_destino_id")))
                .destinationFullName((String) row.get("tercero_destino_nombre"))
                .relationTypeId(toLong(row.get("tipo_relacion_id")))
                .relationTypeName((String) row.get("tipo_relacion_nombre"))
                .isResponsible((Boolean) row.get("es_responsable"))
                .isEmergencyContact((Boolean) row.get("es_contacto_emergencia"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private RelacionTerceroTableDto mapToTableDto(Map<String, Object> row) {
        return RelacionTerceroTableDto.builder()
                .id(toLong(row.get("id")))
                .sourceFullName((String) row.get("tercero_origen_nombre"))
                .destinationFullName((String) row.get("tercero_destino_nombre"))
                .relationTypeName((String) row.get("tipo_relacion_nombre"))
                .isResponsible((Boolean) row.get("es_responsable"))
                .isEmergencyContact((Boolean) row.get("es_contacto_emergencia"))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        return ((Number) v).longValue();
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

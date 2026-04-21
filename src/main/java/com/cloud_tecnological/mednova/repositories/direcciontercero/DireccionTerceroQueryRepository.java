package com.cloud_tecnological.mednova.repositories.direcciontercero;

import com.cloud_tecnological.mednova.dto.direcciontercero.DireccionTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.DireccionTerceroTableDto;
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
public class DireccionTerceroQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DireccionTerceroQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<DireccionTerceroResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT dt.id,
                       dt.empresa_id,
                       dt.tercero_id,
                       dt.tipo_direccion,
                       dt.zona_residencia_id,
                       dt.pais_id,
                       dt.departamento_id,
                       dep.nombre AS departamento_nombre,
                       dt.municipio_id,
                       m.nombre   AS municipio_nombre,
                       dt.direccion,
                       dt.barrio,
                       dt.codigo_postal,
                       dt.referencia,
                       dt.latitud,
                       dt.longitud,
                       dt.es_principal,
                       dt.activo,
                       dt.created_at
                FROM direccion_tercero dt
                INNER JOIN municipio m     ON m.id   = dt.municipio_id
                INNER JOIN departamento dep ON dep.id = dt.departamento_id
                WHERE dt.id = :id
                  AND dt.empresa_id = :empresa_id
                  AND dt.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public List<DireccionTerceroTableDto> listByThirdParty(Long tercero_id, Long empresa_id) {
        String sql = """
                SELECT dt.id,
                       dt.tipo_direccion,
                       m.nombre AS municipio_nombre,
                       dt.direccion,
                       dt.es_principal,
                       dt.activo
                FROM direccion_tercero dt
                INNER JOIN municipio m ON m.id = dt.municipio_id
                WHERE dt.tercero_id = :tercero_id
                  AND dt.empresa_id = :empresa_id
                  AND dt.deleted_at IS NULL
                ORDER BY dt.es_principal DESC, dt.tipo_direccion ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("empresa_id", empresa_id);
        return jdbc.query(sql, params, new ColumnMapRowMapper()).stream()
                .map(this::mapToTableDto).toList();
    }

    public void unmarkPrincipal(Long tercero_id, String tipo_direccion, Long empresa_id) {
        String sql = """
                UPDATE direccion_tercero
                SET es_principal = false
                WHERE tercero_id    = :tercero_id
                  AND tipo_direccion = :tipo_direccion
                  AND empresa_id    = :empresa_id
                  AND deleted_at IS NULL
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("tipo_direccion", tipo_direccion)
                .addValue("empresa_id", empresa_id));
    }

    private DireccionTerceroResponseDto mapToResponseDto(Map<String, Object> row) {
        return DireccionTerceroResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .addressType((String) row.get("tipo_direccion"))
                .residenceZoneId(toLong(row.get("zona_residencia_id")))
                .countryId(toLong(row.get("pais_id")))
                .departmentId(toLong(row.get("departamento_id")))
                .departmentName((String) row.get("departamento_nombre"))
                .municipalityId(toLong(row.get("municipio_id")))
                .municipalityName((String) row.get("municipio_nombre"))
                .address((String) row.get("direccion"))
                .neighborhood((String) row.get("barrio"))
                .postalCode((String) row.get("codigo_postal"))
                .reference((String) row.get("referencia"))
                .latitude(toBigDecimal(row.get("latitud")))
                .longitude(toBigDecimal(row.get("longitud")))
                .isPrincipal((Boolean) row.get("es_principal"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private DireccionTerceroTableDto mapToTableDto(Map<String, Object> row) {
        return DireccionTerceroTableDto.builder()
                .id(toLong(row.get("id")))
                .addressType((String) row.get("tipo_direccion"))
                .municipalityName((String) row.get("municipio_nombre"))
                .address((String) row.get("direccion"))
                .isPrincipal((Boolean) row.get("es_principal"))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        return ((Number) v).longValue();
    }

    private BigDecimal toBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        return new BigDecimal(v.toString());
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

package com.cloud_tecnological.mednova.repositories.contactotercero;

import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.contactotercero.ContactoTerceroTableDto;
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
public class ContactoTerceroQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ContactoTerceroQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ContactoTerceroResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT ct.id,
                       ct.empresa_id,
                       ct.tercero_id,
                       ct.tipo_contacto_id,
                       tc.nombre AS tipo_contacto_nombre,
                       ct.valor,
                       ct.es_principal,
                       ct.acepta_notificaciones,
                       ct.activo,
                       ct.created_at
                FROM contacto_tercero ct
                INNER JOIN tipo_contacto tc ON tc.id = ct.tipo_contacto_id
                WHERE ct.id = :id
                  AND ct.empresa_id = :empresa_id
                  AND ct.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public List<ContactoTerceroTableDto> listByThirdParty(Long tercero_id, Long empresa_id) {
        String sql = """
                SELECT ct.id,
                       tc.nombre AS tipo_contacto_nombre,
                       ct.valor,
                       ct.es_principal,
                       ct.acepta_notificaciones,
                       ct.activo
                FROM contacto_tercero ct
                INNER JOIN tipo_contacto tc ON tc.id = ct.tipo_contacto_id
                WHERE ct.tercero_id = :tercero_id
                  AND ct.empresa_id = :empresa_id
                  AND ct.deleted_at IS NULL
                ORDER BY ct.es_principal DESC, tc.nombre ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("empresa_id", empresa_id);
        return jdbc.query(sql, params, new ColumnMapRowMapper()).stream()
                .map(this::mapToTableDto).toList();
    }

    public void unmarkPrincipal(Long tercero_id, Long tipo_contacto_id, Long empresa_id) {
        String sql = """
                UPDATE contacto_tercero
                SET es_principal = false
                WHERE tercero_id       = :tercero_id
                  AND tipo_contacto_id = :tipo_contacto_id
                  AND empresa_id       = :empresa_id
                  AND deleted_at IS NULL
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("tipo_contacto_id", tipo_contacto_id)
                .addValue("empresa_id", empresa_id));
    }

    private ContactoTerceroResponseDto mapToResponseDto(Map<String, Object> row) {
        return ContactoTerceroResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .contactTypeId(toLong(row.get("tipo_contacto_id")))
                .contactTypeName((String) row.get("tipo_contacto_nombre"))
                .value((String) row.get("valor"))
                .isPrincipal((Boolean) row.get("es_principal"))
                .acceptsNotifications((Boolean) row.get("acepta_notificaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private ContactoTerceroTableDto mapToTableDto(Map<String, Object> row) {
        return ContactoTerceroTableDto.builder()
                .id(toLong(row.get("id")))
                .contactTypeName((String) row.get("tipo_contacto_nombre"))
                .value((String) row.get("valor"))
                .isPrincipal((Boolean) row.get("es_principal"))
                .acceptsNotifications((Boolean) row.get("acepta_notificaciones"))
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

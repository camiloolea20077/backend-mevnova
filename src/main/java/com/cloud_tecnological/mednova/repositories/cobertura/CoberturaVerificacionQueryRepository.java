package com.cloud_tecnological.mednova.repositories.cobertura;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CoberturaVerificacionQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CoberturaVerificacionQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Map<String, Object>> findCoverageForService(Long paciente_id, Long servicio_salud_id, Long empresa_id) {
        String sql = """
                SELECT sc.contrato_id, c.numero AS contrato_numero,
                       sc.requiere_autorizacion, sc.cantidad_maxima
                FROM servicio_contrato sc
                INNER JOIN contrato c ON c.id = sc.contrato_id
                    AND c.empresa_id = :empresa_id
                    AND c.deleted_at IS NULL
                    AND c.activo = true
                INNER JOIN contrato_paciente cp ON cp.contrato_id = sc.contrato_id
                    AND cp.paciente_id = :paciente_id
                    AND cp.empresa_id = :empresa_id
                    AND cp.activo = true
                    AND cp.vigente = true
                WHERE sc.servicio_salud_id = :servicio_salud_id
                  AND sc.empresa_id = :empresa_id
                  AND sc.activo = true
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("paciente_id", paciente_id)
                .addValue("servicio_salud_id", servicio_salud_id)
                .addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}

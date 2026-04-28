package com.cloud_tecnological.mednova.repositories.liquidacion;

import com.cloud_tecnological.mednova.entity.AcumuladoCobroPacienteEntity;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AcumuladoCobroPacienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AcumuladoCobroPacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Map<String, Object>> findAcumulado(Long pacienteId, Long empresaId, Integer vigencia, String tipoCobro) {
        String sql = """
            SELECT id, valor_acumulado_evento, valor_acumulado_anual, tope_evento_aplicado, tope_anual_aplicado
            FROM acumulado_cobro_paciente
            WHERE paciente_id = :paciente_id
              AND empresa_id = :empresa_id
              AND vigencia = :vigencia
              AND tipo_cobro = :tipo_cobro
              AND activo = true
              AND deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("paciente_id", pacienteId)
            .addValue("empresa_id", empresaId)
            .addValue("vigencia", vigencia)
            .addValue("tipo_cobro", tipoCobro);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}

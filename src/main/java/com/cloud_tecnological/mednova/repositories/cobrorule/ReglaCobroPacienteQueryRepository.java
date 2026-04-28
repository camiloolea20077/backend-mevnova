package com.cloud_tecnological.mednova.repositories.cobrorule;

import com.cloud_tecnological.mednova.dto.cobrorule.CobroRuleResponseDto;
import com.cloud_tecnological.mednova.dto.cobrorule.CobroRuleTableDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ReglaCobroPacienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ReglaCobroPacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<CobroRuleResponseDto> findActiveById(Long id, Long empresaId) {
        String sql = """
            SELECT
                r.id,
                r.empresa_id,
                r.vigencia,
                r.regimen_id,
                re.nombre                AS regimen_nombre,
                r.tipo_cobro,
                r.rango_ingreso_desde,
                r.rango_ingreso_hasta,
                r.categoria_sisben_id,
                gs.nombre                AS sisben_nombre,
                r.porcentaje_cobro,
                r.valor_fijo,
                r.tope_evento,
                r.tope_anual,
                r.unidad_valor,
                r.observaciones,
                r.activo,
                r.created_at
            FROM regla_cobro_paciente r
            INNER JOIN regimen re         ON re.id = r.regimen_id
            LEFT JOIN grupo_sisben gs     ON gs.id = r.categoria_sisben_id
            WHERE r.id = :id
              AND r.empresa_id = :empresa_id
              AND r.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<CobroRuleResponseDto> mapped = MapperRepository.mapListToDtoListNull(rows, CobroRuleResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public PageImpl<CobroRuleTableDto> listActive(PageableDto<?> request, Long empresaId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize = request.getRows() != null ? request.getRows().intValue() : 10;

        String sql = """
            SELECT
                r.id,
                r.vigencia,
                re.nombre                AS regimen_nombre,
                r.tipo_cobro,
                r.porcentaje_cobro,
                r.valor_fijo,
                r.tope_evento,
                r.tope_anual,
                r.activo,
                COUNT(*) OVER()          AS total_rows
            FROM regla_cobro_paciente r
            INNER JOIN regimen re ON re.id = r.regimen_id
            WHERE r.empresa_id = :empresa_id
              AND r.deleted_at IS NULL
            ORDER BY r.vigencia DESC, r.tipo_cobro, r.id
            OFFSET :offset LIMIT :limit
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("offset", (long) pageNumber * pageSize)
            .addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        List<CobroRuleTableDto> result = MapperRepository.mapListToDtoListNull(rows, CobroRuleTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();

        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }

    // Busca la regla activa para el cálculo (HU-056)
    public Optional<Map<String, Object>> findActiveRule(Long empresaId, Integer vigencia, Long regimenId,
                                                         String tipoCobro, BigDecimal rangoIngreso, Long categoriaSisbenId) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                r.id,
                r.porcentaje_cobro,
                r.valor_fijo,
                r.tope_evento,
                r.tope_anual,
                r.unidad_valor
            FROM regla_cobro_paciente r
            WHERE r.empresa_id = :empresa_id
              AND r.vigencia = :vigencia
              AND r.regimen_id = :regimen_id
              AND r.tipo_cobro = :tipo_cobro
              AND r.activo = true
              AND r.deleted_at IS NULL
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("vigencia", vigencia)
            .addValue("regimen_id", regimenId)
            .addValue("tipo_cobro", tipoCobro);

        if (rangoIngreso != null) {
            sql.append(" AND (r.rango_ingreso_desde IS NULL OR r.rango_ingreso_desde <= :ingreso)");
            sql.append(" AND (r.rango_ingreso_hasta IS NULL OR r.rango_ingreso_hasta >= :ingreso)");
            params.addValue("ingreso", rangoIngreso);
        }

        if (categoriaSisbenId != null) {
            sql.append(" AND (r.categoria_sisben_id IS NULL OR r.categoria_sisben_id = :sisben_id)");
            params.addValue("sisben_id", categoriaSisbenId);
        }

        sql.append(" ORDER BY r.categoria_sisben_id NULLS LAST, r.rango_ingreso_desde NULLS LAST LIMIT 1");

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public boolean existsDuplicateActiveRule(Long empresaId, Integer vigencia, Long regimenId,
                                              String tipoCobro, Long excludeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*) FROM regla_cobro_paciente
            WHERE empresa_id = :empresa_id
              AND vigencia = :vigencia
              AND regimen_id = :regimen_id
              AND tipo_cobro = :tipo_cobro
              AND activo = true
              AND deleted_at IS NULL
        """);
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("vigencia", vigencia)
            .addValue("regimen_id", regimenId)
            .addValue("tipo_cobro", tipoCobro);

        if (excludeId != null) {
            sql.append(" AND id != :exclude_id");
            params.addValue("exclude_id", excludeId);
        }

        Long count = jdbc.queryForObject(sql.toString(), params, Long.class);
        return count != null && count > 0;
    }
}

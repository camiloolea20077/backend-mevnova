package com.cloud_tecnological.mednova.repositories.cobrorule;

import com.cloud_tecnological.mednova.dto.cobrorule.ServiceExemptionResponseDto;
import com.cloud_tecnological.mednova.dto.cobrorule.ServiceExemptionTableDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ServicioExentoCobroQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ServicioExentoCobroQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isServiceExempt(Long servicioSaludId, String tipoCobro, Long empresaId, LocalDate fecha) {
        String sql = """
            SELECT COUNT(*) FROM servicio_exento_cobro
            WHERE servicio_salud_id = :servicio_salud_id
              AND tipo_cobro = :tipo_cobro
              AND empresa_id = :empresa_id
              AND activo = true
              AND deleted_at IS NULL
              AND vigencia_desde <= :fecha
              AND (vigencia_hasta IS NULL OR vigencia_hasta >= :fecha)
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("servicio_salud_id", servicioSaludId)
            .addValue("tipo_cobro", tipoCobro)
            .addValue("empresa_id", empresaId)
            .addValue("fecha", java.sql.Date.valueOf(fecha));
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public Optional<ServiceExemptionResponseDto> findActiveById(Long id, Long empresaId) {
        String sql = """
            SELECT
                se.id,
                se.empresa_id,
                se.servicio_salud_id,
                ss.nombre                AS service_name,
                se.tipo_cobro,
                se.motivo_exencion,
                se.vigencia_desde,
                se.vigencia_hasta,
                se.activo,
                se.created_at
            FROM servicio_exento_cobro se
            INNER JOIN servicio_salud ss ON ss.id = se.servicio_salud_id
            WHERE se.id = :id
              AND se.empresa_id = :empresa_id
              AND se.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<ServiceExemptionResponseDto> mapped = MapperRepository.mapListToDtoListNull(rows, ServiceExemptionResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public PageImpl<ServiceExemptionTableDto> listActive(PageableDto<?> request, Long empresaId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize = request.getRows() != null ? request.getRows().intValue() : 10;

        String sql = """
            SELECT
                se.id,
                se.servicio_salud_id,
                ss.nombre                AS service_name,
                se.tipo_cobro,
                se.motivo_exencion,
                se.vigencia_desde,
                se.vigencia_hasta,
                se.activo,
                COUNT(*) OVER()          AS total_rows
            FROM servicio_exento_cobro se
            INNER JOIN servicio_salud ss ON ss.id = se.servicio_salud_id
            WHERE se.empresa_id = :empresa_id
              AND se.deleted_at IS NULL
            ORDER BY se.vigencia_desde DESC, se.id
            OFFSET :offset LIMIT :limit
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("offset", (long) pageNumber * pageSize)
            .addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        List<ServiceExemptionTableDto> result = MapperRepository.mapListToDtoListNull(rows, ServiceExemptionTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();

        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }
}

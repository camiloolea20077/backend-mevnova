package com.cloud_tecnological.mednova.repositories.cuentaporcobrar;

import com.cloud_tecnological.mednova.dto.cuentaporcobrar.CuentaPorCobrarResponseDto;
import com.cloud_tecnological.mednova.dto.cuentaporcobrar.CuentaPorCobrarTableDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CuentaPorCobrarQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CuentaPorCobrarQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoCarteraIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_cartera WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public Optional<CuentaPorCobrarResponseDto> findActiveById(Long id, Long empresaId) {
        String sql = """
            SELECT
                c.id,
                c.empresa_id,
                c.sede_id,
                c.factura_id,
                f.numero                 AS invoice_number,
                c.pagador_id,
                t.nombre_completo        AS payer_name,
                c.estado_cartera_id,
                ec.codigo                AS estado_code,
                ec.nombre                AS estado_name,
                c.fecha_causacion,
                c.fecha_vencimiento,
                c.valor_inicial,
                c.saldo                  AS saldo_actual,
                c.dias_mora,
                c.observaciones,
                c.activo,
                c.created_at
            FROM cuenta_por_cobrar c
            INNER JOIN factura f          ON f.id = c.factura_id
            INNER JOIN pagador pag        ON pag.id = c.pagador_id
            INNER JOIN tercero t          ON t.id = pag.tercero_id
            INNER JOIN estado_cartera ec  ON ec.id = c.estado_cartera_id
            WHERE c.id = :id
              AND c.empresa_id = :empresa_id
              AND c.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<CuentaPorCobrarResponseDto> mapped = MapperRepository.mapListToDtoListNull(rows, CuentaPorCobrarResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public Optional<CuentaPorCobrarResponseDto> findByFactura(Long facturaId, Long empresaId) {
        String sql = """
            SELECT
                c.id,
                c.empresa_id,
                c.sede_id,
                c.factura_id,
                f.numero                 AS invoice_number,
                c.pagador_id,
                t.nombre_completo        AS payer_name,
                c.estado_cartera_id,
                ec.codigo                AS estado_code,
                ec.nombre                AS estado_name,
                c.fecha_causacion,
                c.fecha_vencimiento,
                c.valor_inicial,
                c.saldo                  AS saldo_actual,
                c.dias_mora,
                c.observaciones,
                c.activo,
                c.created_at
            FROM cuenta_por_cobrar c
            INNER JOIN factura f          ON f.id = c.factura_id
            INNER JOIN pagador pag        ON pag.id = c.pagador_id
            INNER JOIN tercero t          ON t.id = pag.tercero_id
            INNER JOIN estado_cartera ec  ON ec.id = c.estado_cartera_id
            WHERE c.factura_id = :factura_id
              AND c.empresa_id = :empresa_id
              AND c.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("factura_id", facturaId)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<CuentaPorCobrarResponseDto> mapped = MapperRepository.mapListToDtoListNull(rows, CuentaPorCobrarResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public PageImpl<CuentaPorCobrarTableDto> listActive(PageableDto<?> request, Long empresaId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize   = request.getRows() != null ? request.getRows().intValue() : 10;
        String search  = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT
                c.id,
                f.numero                 AS invoice_number,
                t.nombre_completo        AS payer_name,
                ec.codigo                AS estado_code,
                ec.nombre                AS estado_name,
                c.fecha_causacion,
                c.fecha_vencimiento,
                c.valor_inicial,
                c.saldo                  AS saldo_actual,
                c.dias_mora,
                c.activo,
                COUNT(*) OVER()          AS total_rows
            FROM cuenta_por_cobrar c
            INNER JOIN factura f          ON f.id = c.factura_id
            INNER JOIN pagador pag        ON pag.id = c.pagador_id
            INNER JOIN tercero t          ON t.id = pag.tercero_id
            INNER JOIN estado_cartera ec  ON ec.id = c.estado_cartera_id
            WHERE c.empresa_id = :empresa_id
              AND c.deleted_at IS NULL
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (f.numero ILIKE :search OR unaccent(LOWER(t.nombre_completo)) ILIKE unaccent(LOWER(:search)))");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = request.getOrder_by() != null ? request.getOrder_by() : "c.fecha_causacion";
        String order   = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<CuentaPorCobrarTableDto> result = MapperRepository.mapListToDtoListNull(rows, CuentaPorCobrarTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();

        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }
}

package com.cloud_tecnological.mednova.repositories.radicacion;

import com.cloud_tecnological.mednova.dto.radicacion.RadicacionResponseDto;
import com.cloud_tecnological.mednova.dto.radicacion.RadicacionTableDto;
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
public class RadicacionQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RadicacionQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoRadicacionIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_radicacion WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public String findEstadoRadicacionCodigoById(Long id) {
        String sql = "SELECT codigo FROM estado_radicacion WHERE id = :id LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("id", id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return (String) rows.get(0).get("codigo");
    }

    public Optional<RadicacionResponseDto> findActiveById(Long id, Long empresaId) {
        String sql = """
            SELECT
                r.id,
                r.empresa_id,
                r.sede_id,
                r.factura_id,
                f.numero                 AS invoice_number,
                r.pagador_id,
                t.nombre_completo        AS payer_name,
                r.estado_radicacion_id,
                er.codigo                AS estado_code,
                er.nombre                AS estado_name,
                r.numero_radicado,
                r.fecha_radicacion,
                r.fecha_limite_respuesta,
                r.fecha_respuesta,
                r.soporte_url,
                r.observaciones,
                r.activo,
                r.created_at
            FROM radicacion r
            INNER JOIN factura f          ON f.id = r.factura_id
            INNER JOIN pagador pag        ON pag.id = r.pagador_id
            INNER JOIN tercero t          ON t.id = pag.tercero_id
            INNER JOIN estado_radicacion er ON er.id = r.estado_radicacion_id
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
        return Optional.of(MapperRepository.mapListToDtoListNull(rows, RadicacionResponseDto.class).get(0));
    }

    public PageImpl<RadicacionTableDto> listActive(PageableDto<?> request, Long empresaId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize   = request.getRows() != null ? request.getRows().intValue() : 10;
        String search  = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT
                r.id,
                f.numero                 AS invoice_number,
                t.nombre_completo        AS payer_name,
                er.codigo                AS estado_code,
                er.nombre                AS estado_name,
                r.numero_radicado,
                r.fecha_radicacion,
                r.fecha_limite_respuesta,
                r.fecha_respuesta,
                r.activo,
                COUNT(*) OVER()          AS total_rows
            FROM radicacion r
            INNER JOIN factura f          ON f.id = r.factura_id
            INNER JOIN pagador pag        ON pag.id = r.pagador_id
            INNER JOIN tercero t          ON t.id = pag.tercero_id
            INNER JOIN estado_radicacion er ON er.id = r.estado_radicacion_id
            WHERE r.empresa_id = :empresa_id
              AND r.deleted_at IS NULL
        """);
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (f.numero ILIKE :search OR r.numero_radicado ILIKE :search OR unaccent(LOWER(t.nombre_completo)) ILIKE unaccent(LOWER(:search)))");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = request.getOrder_by() != null ? request.getOrder_by() : "r.fecha_radicacion";
        String order   = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<RadicacionTableDto> result = MapperRepository.mapListToDtoListNull(rows, RadicacionTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();
        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }
}

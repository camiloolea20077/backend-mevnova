package com.cloud_tecnological.mednova.repositories.calendario;

import com.cloud_tecnological.mednova.dto.calendario.CalendarioTableDto;
import com.cloud_tecnological.mednova.dto.calendario.CalendarioResponseDto;
import com.cloud_tecnological.mednova.dto.calendario.DetalleCalendarioResponseDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CalendarioCitaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CalendarioCitaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsByCodigo(String codigo, Long empresaId) {
        String sql = "SELECT COUNT(*) FROM calendario_cita WHERE codigo = :codigo AND empresa_id = :empresa_id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
            .addValue("codigo", codigo)
            .addValue("empresa_id", empresaId), Long.class);
        return count != null && count > 0;
    }

    public Optional<CalendarioResponseDto> findActiveById(Long id, Long empresaId) {
        String sql = """
            SELECT id, codigo, nombre, descripcion, activo, created_at
            FROM calendario_cita
            WHERE id = :id AND empresa_id = :empresa_id AND activo = true
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<CalendarioResponseDto> mapped = MapperRepository.mapListToDtoList(rows, CalendarioResponseDto.class);
        CalendarioResponseDto dto = mapped.get(0);

        String detSql = """
            SELECT id,
                   fecha,
                   es_habil    AS "esHabil",
                   es_festivo  AS "esFestivo",
                   descripcion
            FROM detalle_calendario_cita
            WHERE calendario_id = :calendario_id
            ORDER BY fecha
        """;
        List<Map<String, Object>> detRows = jdbc.query(detSql,
            new MapSqlParameterSource("calendario_id", id), new ColumnMapRowMapper());
        List<DetalleCalendarioResponseDto> detalles = MapperRepository.mapListToDtoList(detRows, DetalleCalendarioResponseDto.class);
        dto.setDetalles(detalles);
        return Optional.of(dto);
    }

    public PageImpl<CalendarioTableDto> listActivos(PageableDto<?> request, Long empresaId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize   = request.getRows() != null ? request.getRows().intValue() : 10;
        String search  = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT id, codigo, nombre, activo, COUNT(*) OVER() AS total_rows
            FROM calendario_cita
            WHERE empresa_id = :empresa_id AND activo = true
        """);

        MapSqlParameterSource params = new MapSqlParameterSource("empresa_id", empresaId);
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (nombre ILIKE :search OR codigo ILIKE :search)");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = request.getOrder_by() != null ? request.getOrder_by() : "nombre";
        String order   = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<CalendarioTableDto> result = MapperRepository.mapListToDtoList(rows, CalendarioTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();
        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }

    public Map<LocalDate, Boolean> findHabilityMap(Long calendarioId, LocalDate from, LocalDate to) {
        String sql = """
            SELECT fecha, es_habil AS "esHabil"
            FROM detalle_calendario_cita
            WHERE calendario_id = :calendario_id AND fecha BETWEEN :from AND :to
        """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
            .addValue("calendario_id", calendarioId)
            .addValue("from", from)
            .addValue("to", to), new ColumnMapRowMapper());
        Map<LocalDate, Boolean> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDate fecha = ((java.sql.Date) row.get("fecha")).toLocalDate();
            map.put(fecha, (Boolean) row.get("esHabil"));
        }
        return map;
    }
}

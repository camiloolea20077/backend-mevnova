package com.cloud_tecnological.mednova.repositories.serviciohabilitado;

import com.cloud_tecnological.mednova.dto.serviciohabilitado.ServicioHabilitadoResponseDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.ServicioHabilitadoTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ServicioHabilitadoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ServicioHabilitadoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ServicioHabilitadoResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT sh.id,
                       sh.sede_id,
                       s.nombre AS sede_nombre,
                       sh.codigo_servicio,
                       sh.nombre_servicio,
                       sh.modalidad,
                       sh.complejidad,
                       sh.fecha_habilitacion,
                       sh.fecha_vencimiento,
                       sh.resolucion,
                       sh.observaciones,
                       sh.activo,
                       sh.created_at,
                       (sh.fecha_vencimiento IS NULL OR sh.fecha_vencimiento >= CURRENT_DATE) AS vigente
                FROM servicio_habilitado sh
                INNER JOIN sede s ON s.id = sh.sede_id
                WHERE sh.id = :id
                  AND sh.empresa_id = :empresa_id
                  AND sh.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public boolean existsActiveBySedeAndCode(Long sede_id, Long empresa_id, String codigo, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM servicio_habilitado
                WHERE sede_id = :sede_id
                  AND empresa_id = :empresa_id
                  AND codigo_servicio = :codigo
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sede_id", sede_id)
                .addValue("empresa_id", empresa_id)
                .addValue("codigo", codigo)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public PageImpl<ServicioHabilitadoTableDto> listServices(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT sh.id,
                       sh.codigo_servicio,
                       sh.nombre_servicio,
                       s.nombre AS sede_nombre,
                       sh.modalidad,
                       sh.complejidad,
                       sh.fecha_habilitacion,
                       sh.fecha_vencimiento,
                       (sh.fecha_vencimiento IS NULL OR sh.fecha_vencimiento >= CURRENT_DATE) AS vigente,
                       sh.activo,
                       COUNT(*) OVER() AS total_rows
                FROM servicio_habilitado sh
                INNER JOIN sede s ON s.id = sh.sede_id
                WHERE sh.empresa_id = :empresa_id
                  AND sh.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(sh.nombre_servicio) LIKE UPPER(:search)
                        OR UPPER(sh.codigo_servicio) LIKE UPPER(:search)
                        OR UPPER(s.nombre) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "sh.nombre_servicio";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<ServicioHabilitadoTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private ServicioHabilitadoResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return ServicioHabilitadoResponseDto.builder()
                .id(toLong(row.get("id")))
                .sedeId(toLong(row.get("sede_id")))
                .sedeName((String) row.get("sede_nombre"))
                .serviceCode((String) row.get("codigo_servicio"))
                .serviceName((String) row.get("nombre_servicio"))
                .modality((String) row.get("modalidad"))
                .complexity((String) row.get("complejidad"))
                .enablementDate(toLocalDate(row.get("fecha_habilitacion")))
                .expirationDate(toLocalDate(row.get("fecha_vencimiento")))
                .resolution((String) row.get("resolucion"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .valid((Boolean) row.get("vigente"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private ServicioHabilitadoTableDto mapRowToTableDto(Map<String, Object> row) {
        return ServicioHabilitadoTableDto.builder()
                .id(toLong(row.get("id")))
                .serviceCode((String) row.get("codigo_servicio"))
                .serviceName((String) row.get("nombre_servicio"))
                .sedeName((String) row.get("sede_nombre"))
                .modality((String) row.get("modalidad"))
                .complexity((String) row.get("complejidad"))
                .enablementDate(toLocalDate(row.get("fecha_habilitacion")))
                .expirationDate(toLocalDate(row.get("fecha_vencimiento")))
                .valid((Boolean) row.get("vigente"))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

package com.cloud_tecnological.mednova.repositories.usuariorol;

import com.cloud_tecnological.mednova.dto.usuariorol.UsuarioRolResponseDto;
import com.cloud_tecnological.mednova.dto.usuariorol.UsuarioRolTableDto;
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
public class UsuarioRolQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public UsuarioRolQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<UsuarioRolResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT ur.id,
                       ur.usuario_id,
                       u.nombre_usuario,
                       ur.rol_id,
                       r.nombre AS rol_nombre,
                       ur.sede_id,
                       s.nombre  AS sede_nombre,
                       ur.activo,
                       ur.fecha_vigencia_desde,
                       ur.fecha_vigencia_hasta,
                       ur.created_at
                FROM usuario_rol ur
                INNER JOIN usuario u ON u.id = ur.usuario_id
                INNER JOIN rol     r ON r.id = ur.rol_id
                LEFT  JOIN sede    s ON s.id = ur.sede_id
                WHERE ur.id = :id
                  AND ur.empresa_id = :empresa_id
                  AND ur.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public boolean existsAssignment(Long usuario_id, Long rol_id, Long sede_id, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM usuario_rol
                WHERE usuario_id = :usuario_id
                  AND rol_id = :rol_id
                  AND empresa_id = :empresa_id
                  AND (ur_sede_match)
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        // Construir la condición de sede manualmente para manejar null
        String sedeCondition = sede_id == null
                ? "sede_id IS NULL"
                : "sede_id = :sede_id";

        String finalSql = """
                SELECT COUNT(*) FROM usuario_rol
                WHERE usuario_id = :usuario_id
                  AND rol_id = :rol_id
                  AND empresa_id = :empresa_id
                  AND """ + sedeCondition + """

                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usuario_id", usuario_id)
                .addValue("rol_id", rol_id)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId);
        if (sede_id != null) params.addValue("sede_id", sede_id);

        Long count = jdbc.queryForObject(finalSql, params, Long.class);
        return count != null && count > 0;
    }

    public PageImpl<UsuarioRolTableDto> listAssignments(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT ur.id,
                       u.nombre_usuario,
                       r.nombre  AS rol_nombre,
                       COALESCE(s.nombre, 'Todas las sedes') AS sede_nombre,
                       ur.activo,
                       ur.fecha_vigencia_desde,
                       ur.fecha_vigencia_hasta,
                       COUNT(*) OVER() AS total_rows
                FROM usuario_rol ur
                INNER JOIN usuario u ON u.id = ur.usuario_id
                INNER JOIN rol     r ON r.id = ur.rol_id
                LEFT  JOIN sede    s ON s.id = ur.sede_id
                WHERE ur.empresa_id = :empresa_id
                  AND ur.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(u.nombre_usuario) LIKE UPPER(:search)
                        OR UPPER(r.nombre) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "u.nombre_usuario";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<UsuarioRolTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    public List<UsuarioRolResponseDto> listByUser(Long usuario_id, Long empresa_id) {
        String sql = """
                SELECT ur.id,
                       ur.usuario_id,
                       u.nombre_usuario,
                       ur.rol_id,
                       r.nombre AS rol_nombre,
                       ur.sede_id,
                       s.nombre  AS sede_nombre,
                       ur.activo,
                       ur.fecha_vigencia_desde,
                       ur.fecha_vigencia_hasta,
                       ur.created_at
                FROM usuario_rol ur
                INNER JOIN usuario u ON u.id = ur.usuario_id
                INNER JOIN rol     r ON r.id = ur.rol_id
                LEFT  JOIN sede    s ON s.id = ur.sede_id
                WHERE ur.usuario_id = :usuario_id
                  AND ur.empresa_id = :empresa_id
                  AND ur.deleted_at IS NULL
                ORDER BY ur.created_at DESC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usuario_id", usuario_id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream().map(this::mapRowToResponseDto).toList();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private UsuarioRolResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return UsuarioRolResponseDto.builder()
                .id(toLong(row.get("id")))
                .userId(toLong(row.get("usuario_id")))
                .username((String) row.get("nombre_usuario"))
                .roleId(toLong(row.get("rol_id")))
                .roleName((String) row.get("rol_nombre"))
                .sedeId(toLong(row.get("sede_id")))
                .sedeName((String) row.get("sede_nombre"))
                .active((Boolean) row.get("activo"))
                .validFrom(toLocalDate(row.get("fecha_vigencia_desde")))
                .validUntil(toLocalDate(row.get("fecha_vigencia_hasta")))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private UsuarioRolTableDto mapRowToTableDto(Map<String, Object> row) {
        return UsuarioRolTableDto.builder()
                .id(toLong(row.get("id")))
                .username((String) row.get("nombre_usuario"))
                .roleName((String) row.get("rol_nombre"))
                .sedeName((String) row.get("sede_nombre"))
                .active((Boolean) row.get("activo"))
                .validFrom(toLocalDate(row.get("fecha_vigencia_desde")))
                .validUntil(toLocalDate(row.get("fecha_vigencia_hasta")))
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

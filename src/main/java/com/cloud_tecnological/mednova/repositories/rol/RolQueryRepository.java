package com.cloud_tecnological.mednova.repositories.rol;

import com.cloud_tecnological.mednova.dto.permiso.PermisoDto;
import com.cloud_tecnological.mednova.dto.rol.RolResponseDto;
import com.cloud_tecnological.mednova.dto.rol.RolTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
public class RolQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RolQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<RolResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT id, codigo, nombre, descripcion, es_global, activo, created_at
                FROM rol
                WHERE id = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        RolResponseDto dto = mapRowToResponseDto(rows.get(0));
        dto.setPermissions(findPermissionsByRol(id));
        return Optional.of(dto);
    }

    public List<PermisoDto> findPermissionsByRol(Long rolId) {
        String sql = """
                SELECT p.id, p.codigo, p.nombre, p.descripcion, p.modulo, p.activo
                FROM rol_permiso rp
                INNER JOIN permiso p ON p.id = rp.permiso_id AND p.activo = true AND p.deleted_at IS NULL
                WHERE rp.rol_id = :rol_id
                ORDER BY p.modulo, p.nombre
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("rol_id", rolId), new ColumnMapRowMapper());
        return rows.stream().map(row -> PermisoDto.builder()
                .id(((Number) row.get("id")).longValue())
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .description((String) row.get("descripcion"))
                .module((String) row.get("modulo"))
                .active((Boolean) row.get("activo"))
                .build()).toList();
    }

    public boolean existsByCodigoAndEmpresa(String codigo, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM rol
                WHERE codigo = :codigo
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("codigo", codigo)
                        .addValue("empresa_id", empresa_id)
                        .addValue("exclude_id", excludeId == null ? -1 : excludeId),
                Long.class);
        return count != null && count > 0;
    }

    public boolean hasActiveUsers(Long rolId, Long empresa_id) {
        String sql = """
                SELECT COUNT(*) FROM usuario_rol
                WHERE rol_id = :rol_id
                  AND empresa_id = :empresa_id
                  AND activo = true
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("rol_id", rolId).addValue("empresa_id", empresa_id),
                Long.class);
        return count != null && count > 0;
    }

    public boolean hasActivePermissions(Long rolId) {
        String sql = """
                SELECT COUNT(*) FROM rol_permiso
                WHERE rol_id = :rol_id AND activo = true
                """;
        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("rol_id", rolId), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<RolTableDto> listRoles(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT r.id,
                       r.codigo,
                       r.nombre,
                       r.activo,
                       COUNT(rp.id) AS permisos_count,
                       COUNT(*) OVER() AS total_rows
                FROM rol r
                LEFT JOIN rol_permiso rp ON rp.rol_id = r.id
                WHERE r.empresa_id = :empresa_id
                  AND r.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("AND (UPPER(r.nombre) LIKE UPPER(:search) OR UPPER(r.codigo) LIKE UPPER(:search)) ");
            params.addValue("search", "%" + search + "%");
        }

        sql.append(" GROUP BY r.id, r.codigo, r.nombre, r.activo");
        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "r.nombre";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<RolTableDto> dtos = result.stream().map(row -> RolTableDto.builder()
                .id(((Number) row.get("id")).longValue())
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .permissionsCount(row.get("permisos_count") != null ? ((Number) row.get("permisos_count")).intValue() : 0)
                .active((Boolean) row.get("activo"))
                .build()).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private RolResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return RolResponseDto.builder()
                .id(((Number) row.get("id")).longValue())
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .description((String) row.get("descripcion"))
                .isGlobal((Boolean) row.get("es_global"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

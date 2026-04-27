package com.cloud_tecnological.mednova.repositories.usuario;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_tecnological.mednova.dto.usuario.UsuarioResponseDto;
import com.cloud_tecnological.mednova.dto.usuario.UsuarioTableDto;
import com.cloud_tecnological.mednova.entity.UsuarioEntity;
import com.cloud_tecnological.mednova.util.PageableDto;

@Repository
public class UsuarioQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public UsuarioQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<UsuarioEntity> findByUsernameAndEmpresa(String username, Long empresaId) {
        String sql = """
                SELECT id, empresa_id, tercero_id, nombre_usuario, correo, hash_password,
                       es_super_admin, requiere_cambio_password, intentos_fallidos,
                       bloqueado, fecha_bloqueo, motivo_bloqueo,
                       fecha_ultimo_ingreso, ip_ultimo_ingreso, activo
                FROM usuario
                WHERE nombre_usuario = :username
                  AND empresa_id = :empresa_id
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", username)
                .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToEntity(rows.get(0)));
    }

    // Incluye roles de sede específica y roles globales (sede_id IS NULL)
    public List<String> findRolesByUserEmpresaSede(Long usuarioId, Long empresaId, Long sedeId) {
        String sql = """
                SELECT DISTINCT r.nombre
                FROM usuario_rol ur
                INNER JOIN rol r ON r.id = ur.rol_id AND r.activo = true
                WHERE ur.usuario_id = :usuario_id
                  AND ur.empresa_id = :empresa_id
                  AND (ur.sede_id = :sede_id OR ur.sede_id IS NULL)
                  AND ur.activo = true
                  AND ur.deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usuario_id", usuarioId)
                .addValue("empresa_id", empresaId)
                .addValue("sede_id", sedeId);
        return jdbc.queryForList(sql, params, String.class);
    }

    // Unión de permisos de todos los roles del usuario para empresa+sede
    public List<String> findPermissionsByUserEmpresaSede(Long usuarioId, Long empresaId, Long sedeId) {
        String sql = """
                SELECT DISTINCT p.nombre
                FROM usuario_rol ur
                INNER JOIN rol r ON r.id = ur.rol_id AND r.activo = true
                INNER JOIN rol_permiso rp ON rp.rol_id = r.id
                INNER JOIN permiso p ON p.id = rp.permiso_id AND p.activo = true
                WHERE ur.usuario_id = :usuario_id
                  AND ur.empresa_id = :empresa_id
                  AND (ur.sede_id = :sede_id OR ur.sede_id IS NULL)
                  AND ur.activo = true
                  AND ur.deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usuario_id", usuarioId)
                .addValue("empresa_id", empresaId)
                .addValue("sede_id", sedeId);
        return jdbc.queryForList(sql, params, String.class);
    }

    public Optional<UsuarioEntity> findSuperAdminByUsername(String username) {
        String sql = """
                SELECT id, empresa_id, tercero_id, nombre_usuario, correo, hash_password,
                       es_super_admin, requiere_cambio_password, intentos_fallidos,
                       bloqueado, fecha_bloqueo, motivo_bloqueo,
                       fecha_ultimo_ingreso, ip_ultimo_ingreso, activo
                FROM usuario
                WHERE nombre_usuario = :username
                  AND es_super_admin = true
                  AND empresa_id IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", username);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToEntity(rows.get(0)));
    }

    // ── HU-FASE1-006: Gestión de usuarios ────────────────────────────────────

    public boolean existsByUsernameAndEmpresa(String username, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM usuario
                WHERE nombre_usuario = :username
                  AND empresa_id = :empresa_id
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("username", username)
                        .addValue("empresa_id", empresa_id)
                        .addValue("exclude_id", excludeId == null ? -1 : excludeId),
                Long.class);
        return count != null && count > 0;
    }

    public boolean existsByEmailAndEmpresa(String email, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM usuario
                WHERE correo = :email
                  AND empresa_id = :empresa_id
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("email", email)
                        .addValue("empresa_id", empresa_id)
                        .addValue("exclude_id", excludeId == null ? -1 : excludeId),
                Long.class);
        return count != null && count > 0;
    }

    public Optional<UsuarioResponseDto> findActiveByIdAndEmpresa(Long id, Long empresa_id) {
        String sql = """
                SELECT id, nombre_usuario, correo, tercero_id, activo, bloqueado,
                       requiere_cambio_password, intentos_fallidos,
                       fecha_ultimo_ingreso, created_at
                FROM usuario
                WHERE id = :id
                  AND empresa_id = :empresa_id
                  AND activo = true
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        Map<String, Object> row = rows.get(0);
        return Optional.of(UsuarioResponseDto.builder()
                .id(toLong(row.get("id")))
                .username((String) row.get("nombre_usuario"))
                .email((String) row.get("correo"))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .active((Boolean) row.get("activo"))
                .blocked((Boolean) row.get("bloqueado"))
                .requiresPasswordChange((Boolean) row.get("requiere_cambio_password"))
                .failedAttempts(toInt(row.get("intentos_fallidos")))
                .lastLogin(toLocalDateTime(row.get("fecha_ultimo_ingreso")))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build());
    }

    public PageImpl<UsuarioTableDto> listUsuarios(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT id, nombre_usuario, correo, activo, bloqueado,
                       COUNT(*) OVER() AS total_rows
                FROM usuario
                WHERE empresa_id = :empresa_id
                  AND es_super_admin = false
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(nombre_usuario) LIKE UPPER(:search)
                        OR UPPER(correo) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String rawOrderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "nombre_usuario";
        // Strip table alias prefix (e.g. "u.nombre_usuario" → "nombre_usuario")
        String col = rawOrderBy.contains(".") ? rawOrderBy.substring(rawOrderBy.lastIndexOf('.') + 1) : rawOrderBy;
        java.util.Set<String> allowed = java.util.Set.of("nombre_usuario", "correo", "activo", "bloqueado", "created_at");
        String orderBy = allowed.contains(col) ? col : "nombre_usuario";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<UsuarioTableDto> dtos = result.stream().map(row -> UsuarioTableDto.builder()
                .id(toLong(row.get("id")))
                .username((String) row.get("nombre_usuario"))
                .email((String) row.get("correo"))
                .active((Boolean) row.get("activo"))
                .blocked((Boolean) row.get("bloqueado"))
                .build()).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private UsuarioEntity mapRowToEntity(Map<String, Object> row) {
        UsuarioEntity u = new UsuarioEntity();
        u.setId(toLong(row.get("id")));
        u.setEmpresa_id(toLong(row.get("empresa_id")));
        u.setTercero_id(toLong(row.get("tercero_id")));
        u.setNombre_usuario((String) row.get("nombre_usuario"));
        u.setCorreo((String) row.get("correo"));
        u.setHash_password((String) row.get("hash_password"));
        u.setEs_super_admin((Boolean) row.get("es_super_admin"));
        u.setRequiere_cambio_password((Boolean) row.get("requiere_cambio_password"));
        u.setIntentos_fallidos(toInt(row.get("intentos_fallidos")));
        u.setBloqueado((Boolean) row.get("bloqueado"));
        u.setFecha_bloqueo(toLocalDateTime(row.get("fecha_bloqueo")));
        u.setMotivo_bloqueo((String) row.get("motivo_bloqueo"));
        u.setFecha_ultimo_ingreso(toLocalDateTime(row.get("fecha_ultimo_ingreso")));
        u.setIp_ultimo_ingreso((String) row.get("ip_ultimo_ingreso"));
        u.setActivo((Boolean) row.get("activo"));
        return u;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private Integer toInt(Object value) {
        if (value == null) return 0;
        return ((Number) value).intValue();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

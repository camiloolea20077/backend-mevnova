package com.cloud_tecnological.mednova.repositories.usuario;

import com.cloud_tecnological.mednova.entity.UsuarioEntity;
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
                INNER JOIN rol_permiso rp ON rp.rol_id = r.id AND rp.activo = true
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

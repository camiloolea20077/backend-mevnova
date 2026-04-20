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
                SELECT id, empresa_id, nombre_usuario, hash_password, nombre_completo,
                       activo, bloqueado, intentos_fallidos, requiere_cambio_password,
                       fecha_ultimo_ingreso, ip_ultimo_ingreso, fecha_bloqueo, motivo_bloqueo
                FROM usuario
                WHERE nombre_usuario = :username
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
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

    private UsuarioEntity mapRowToEntity(Map<String, Object> row) {
        UsuarioEntity u = new UsuarioEntity();
        u.setId(toLong(row.get("id")));
        u.setEmpresa_id(toLong(row.get("empresa_id")));
        u.setNombre_usuario((String) row.get("nombre_usuario"));
        u.setHash_password((String) row.get("hash_password"));
        u.setNombre_completo((String) row.get("nombre_completo"));
        u.setActivo((Boolean) row.get("activo"));
        u.setBloqueado((Boolean) row.get("bloqueado"));
        u.setIntentos_fallidos(toInt(row.get("intentos_fallidos")));
        u.setRequiere_cambio_password((Boolean) row.get("requiere_cambio_password"));
        u.setFecha_ultimo_ingreso(toLocalDateTime(row.get("fecha_ultimo_ingreso")));
        u.setIp_ultimo_ingreso((String) row.get("ip_ultimo_ingreso"));
        u.setFecha_bloqueo(toLocalDateTime(row.get("fecha_bloqueo")));
        u.setMotivo_bloqueo((String) row.get("motivo_bloqueo"));
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

package com.cloud_tecnological.mednova.repositories.auth;

import com.cloud_tecnological.mednova.entity.SesionUsuarioEntity;
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
public class SesionUsuarioQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SesionUsuarioQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<SesionUsuarioEntity> findByJti(String jti) {
        String sql = """
                SELECT id, jti, parent_jti, tipo_token, empresa_id, usuario_id, sede_id,
                       usado, ip, fecha_expiracion, fecha_uso, fecha_revocacion, created_at
                FROM sesion_usuario
                WHERE jti = :jti
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("jti", jti), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToEntity(rows.get(0)));
    }

    public Optional<SesionUsuarioEntity> findByParentJti(String parentJti) {
        String sql = """
                SELECT id, jti, parent_jti, tipo_token, empresa_id, usuario_id, sede_id,
                       usado, ip, fecha_expiracion, fecha_uso, fecha_revocacion, created_at
                FROM sesion_usuario
                WHERE parent_jti = :parent_jti
                  AND fecha_revocacion IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("parent_jti", parentJti), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToEntity(rows.get(0)));
    }

    // Usado por JwtAuthenticationFilter en cada request autenticado
    public boolean isRevoked(String jti) {
        String sql = """
                SELECT COUNT(*)
                FROM sesion_usuario
                WHERE jti = :jti
                  AND (fecha_revocacion IS NOT NULL OR usado = true)
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("jti", jti), Long.class);
        return count != null && count > 0;
    }

    private SesionUsuarioEntity mapRowToEntity(Map<String, Object> row) {
        SesionUsuarioEntity s = new SesionUsuarioEntity();
        s.setId(toLong(row.get("id")));
        s.setJti((String) row.get("jti"));
        s.setParent_jti((String) row.get("parent_jti"));
        s.setTipo_token((String) row.get("tipo_token"));
        s.setEmpresa_id(toLong(row.get("empresa_id")));
        s.setUsuario_id(toLong(row.get("usuario_id")));
        s.setSede_id(toLong(row.get("sede_id")));
        s.setUsado((Boolean) row.get("usado"));
        s.setIp((String) row.get("ip"));
        s.setFecha_expiracion(toLocalDateTime(row.get("fecha_expiracion")));
        s.setFecha_uso(toLocalDateTime(row.get("fecha_uso")));
        s.setFecha_revocacion(toLocalDateTime(row.get("fecha_revocacion")));
        return s;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

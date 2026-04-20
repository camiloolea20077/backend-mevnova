package com.cloud_tecnological.mednova.repositories.auth;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HistorialPasswordQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public HistorialPasswordQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<String> findRecentHashes(Long usuarioId, int limit) {
        String sql = """
                SELECT hash_password
                FROM historial_password
                WHERE usuario_id = :usuario_id
                ORDER BY fecha_cambio DESC
                LIMIT :limit
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usuario_id", usuarioId)
                .addValue("limit", limit);
        return jdbc.queryForList(sql, params, String.class);
    }
}

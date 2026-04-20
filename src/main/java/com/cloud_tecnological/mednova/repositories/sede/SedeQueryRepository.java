package com.cloud_tecnological.mednova.repositories.sede;

import com.cloud_tecnological.mednova.dto.auth.SedeDto;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SedeQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SedeQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SedeDto> findAvailableByUserAndEmpresa(Long usuarioId, Long empresaId) {
        String sql = """
                SELECT DISTINCT s.id, s.codigo, s.nombre
                FROM usuario_rol ur
                INNER JOIN sede s ON s.id = ur.sede_id
                    AND s.activo = true
                    AND s.deleted_at IS NULL
                WHERE ur.usuario_id = :usuario_id
                  AND ur.empresa_id = :empresa_id
                  AND ur.activo = true
                  AND ur.deleted_at IS NULL
                ORDER BY s.nombre ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usuario_id", usuarioId)
                .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream()
                .map(row -> SedeDto.builder()
                        .id(((Number) row.get("id")).longValue())
                        .codigo((String) row.get("codigo"))
                        .nombre((String) row.get("nombre"))
                        .build())
                .toList();
    }

    public boolean existsActiveByIdAndEmpresa(Long sedeId, Long empresaId) {
        String sql = """
                SELECT COUNT(*)
                FROM sede
                WHERE id = :sede_id
                  AND empresa_id = :empresa_id
                  AND activo = true
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sede_id", sedeId)
                .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }
}

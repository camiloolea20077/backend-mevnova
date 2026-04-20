package com.cloud_tecnological.mednova.repositories.empresa;

import com.cloud_tecnological.mednova.entity.EmpresaEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.ColumnMapRowMapper;

@Repository
public class EmpresaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EmpresaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<EmpresaEntity> findActiveByCodigo(String codigo) {
        String sql = """
                SELECT id, codigo, nit, nombre_comercial, razon_social, logo_url, activo
                FROM empresa
                WHERE codigo = :codigo
                  AND activo = true
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("codigo", codigo);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        return Optional.of(mapRowToEntity(rows.get(0)));
    }

    public Optional<EmpresaEntity> findActiveByNit(String nit) {
        String sql = """
                SELECT id, codigo, nit, nombre_comercial, razon_social, logo_url, activo
                FROM empresa
                WHERE nit = :nit
                  AND activo = true
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nit", nit);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        return Optional.of(mapRowToEntity(rows.get(0)));
    }

    private EmpresaEntity mapRowToEntity(Map<String, Object> row) {
        EmpresaEntity e = new EmpresaEntity();
        e.setId(((Number) row.get("id")).longValue());
        e.setCodigo((String) row.get("codigo"));
        e.setNit((String) row.get("nit"));
        e.setNombre_comercial((String) row.get("nombre_comercial"));
        e.setRazon_social((String) row.get("razon_social"));
        e.setLogo_url((String) row.get("logo_url"));
        e.setActivo((Boolean) row.get("activo"));
        return e;
    }
}

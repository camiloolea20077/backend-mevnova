package com.cloud_tecnological.mednova.repositories.diagnostico;

import com.cloud_tecnological.mednova.dto.diagnostico.CatalogoDiagnosticoSearchDto;
import com.cloud_tecnological.mednova.dto.diagnostico.DiagnosticoResponseDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DiagnosticoAtencionQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DiagnosticoAtencionQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<DiagnosticoResponseDto> findByAtencionId(Long atencionId, Long empresaId) {
        String sql = """
            SELECT
                da.id,
                da.atencion_id,
                da.empresa_id,
                da.catalogo_diagnostico_id,
                cd.codigo          AS diagnostico_codigo,
                cd.nombre          AS diagnostico_nombre,
                cd.capitulo,
                da.tipo_diagnostico,
                da.es_confirmado,
                da.observaciones,
                da.activo,
                da.created_at
            FROM diagnostico_atencion da
            INNER JOIN catalogo_diagnostico cd ON cd.id = da.catalogo_diagnostico_id
            WHERE da.atencion_id = :atencion_id
              AND da.empresa_id  = :empresa_id
              AND da.activo      = true
            ORDER BY
                CASE da.tipo_diagnostico WHEN 'PRINCIPAL' THEN 1 ELSE 2 END,
                da.id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("atencion_id", atencionId)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return MapperRepository.mapListToDtoList(rows, DiagnosticoResponseDto.class);
    }

    public boolean existsPrincipalByAtencion(Long atencionId, Long empresaId) {
        String sql = """
            SELECT COUNT(*)
            FROM diagnostico_atencion
            WHERE atencion_id      = :atencion_id
              AND empresa_id       = :empresa_id
              AND tipo_diagnostico = 'PRINCIPAL'
              AND activo           = true
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("atencion_id", atencionId)
            .addValue("empresa_id", empresaId);

        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean catalogoDiagnosticoExists(Long catalogoId) {
        String sql = "SELECT COUNT(*) FROM catalogo_diagnostico WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", catalogoId), Long.class);
        return count != null && count > 0;
    }

    public List<CatalogoDiagnosticoSearchDto> searchCatalogo(String search, int limit) {
        String sql = """
            SELECT
                id,
                codigo,
                nombre,
                capitulo,
                activo
            FROM catalogo_diagnostico
            WHERE activo = true
              AND (
                  LOWER(nombre) ILIKE LOWER(:search)
                  OR UPPER(codigo) LIKE UPPER(:exact_search)
              )
            ORDER BY codigo
            LIMIT :limit
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("search", "%" + search.trim() + "%")
            .addValue("exact_search", search.trim() + "%")
            .addValue("limit", limit);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return MapperRepository.mapListToDtoList(rows, CatalogoDiagnosticoSearchDto.class);
    }
}

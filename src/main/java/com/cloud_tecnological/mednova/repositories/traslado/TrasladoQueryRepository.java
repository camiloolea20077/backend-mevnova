package com.cloud_tecnological.mednova.repositories.traslado;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrasladoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TrasladoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Bodega activa de la empresa (sin restricción de sede). Devuelve id, sede_id, nombre,
     * permite_dispensar, permite_recibir.
     */
    public Optional<Map<String, Object>> findBodegaInEmpresa(Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT id,
                       sede_id,
                       nombre,
                       permite_dispensar,
                       permite_recibir
                FROM bodega
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND activo     = true
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", bodega_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Lote de la empresa. Devuelve id, numero_lote, fecha_vencimiento, servicio_salud_id,
     * servicio_nombre.
     */
    public Optional<Map<String, Object>> findLoteInEmpresa(Long lote_id, Long empresa_id) {
        String sql = """
                SELECT l.id,
                       l.numero_lote,
                       l.fecha_vencimiento,
                       l.servicio_salud_id,
                       sv.nombre AS servicio_nombre
                FROM lote l
                INNER JOIN servicio_salud sv ON sv.id = l.servicio_salud_id
                WHERE l.id         = :id
                  AND l.empresa_id = :empresa_id
                  AND l.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", lote_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Stock del lote en la bodega indicada: id (stock_lote), cantidad_disponible.
     */
    public Optional<Map<String, Object>> findStockLoteInBodega(Long lote_id, Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT id AS stock_lote_id, cantidad_disponible
                FROM stock_lote
                WHERE lote_id    = :lote_id
                  AND bodega_id  = :bodega_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lote_id", lote_id)
                .addValue("bodega_id", bodega_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}

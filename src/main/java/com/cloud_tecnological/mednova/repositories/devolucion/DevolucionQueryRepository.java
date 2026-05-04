package com.cloud_tecnological.mednova.repositories.devolucion;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DevolucionQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DevolucionQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Devuelve dispensación de la sede del tenant: id, estado, numero_dispensacion.
     */
    public Optional<Map<String, Object>> findDispensacion(Long dispensacion_id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT d.id,
                       d.estado,
                       d.numero_dispensacion
                FROM dispensacion d
                WHERE d.id         = :id
                  AND d.empresa_id = :empresa_id
                  AND d.sede_id    = :sede_id
                  AND d.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", dispensacion_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Devuelve datos del detalle de dispensación enriquecidos con lote y servicio.
     * Mapa con: id, lote_id, numero_lote, fecha_vencimiento, servicio_salud_id,
     *           servicio_nombre, cantidad.
     */
    public Optional<Map<String, Object>> findDetalleDispensacion(Long detalle_id, Long dispensacion_id, Long empresa_id) {
        String sql = """
                SELECT dd.id,
                       dd.lote_id,
                       l.numero_lote,
                       l.fecha_vencimiento,
                       dd.servicio_salud_id,
                       sv.nombre AS servicio_nombre,
                       dd.cantidad
                FROM detalle_dispensacion dd
                INNER JOIN lote l            ON l.id  = dd.lote_id
                INNER JOIN servicio_salud sv ON sv.id = dd.servicio_salud_id
                WHERE dd.id              = :detalle_id
                  AND dd.dispensacion_id = :dispensacion_id
                  AND dd.empresa_id      = :empresa_id
                  AND dd.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("detalle_id", detalle_id)
                .addValue("dispensacion_id", dispensacion_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Suma cantidad ya devuelta o dada de baja contra un detalle de dispensación específico.
     */
    public BigDecimal sumReturnedQuantityByDetalleDispensacion(Long detalle_id, Long empresa_id) {
        String sql = """
                SELECT COALESCE(SUM(cantidad), 0) AS total_devuelto
                FROM movimiento_inventario
                WHERE empresa_id        = :empresa_id
                  AND referencia_tipo   = 'DEVOLUCION_DISPENSACION'
                  AND referencia_id     = :detalle_id
                  AND tipo_movimiento  IN ('DEVOLUCION_PACIENTE','BAJA_VENCIMIENTO')
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("detalle_id", detalle_id)
                .addValue("empresa_id", empresa_id);
        BigDecimal total = jdbc.queryForObject(sql, params, BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    public boolean existsBodegaActivaPermiteRecibir(Long bodega_id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT COUNT(*)
                FROM bodega
                WHERE id          = :bodega_id
                  AND empresa_id  = :empresa_id
                  AND sede_id     = :sede_id
                  AND activo      = true
                  AND permite_recibir = true
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("bodega_id", bodega_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

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

    public Optional<String> findBodegaNombre(Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT nombre
                FROM bodega
                WHERE id = :id AND empresa_id = :empresa_id AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", bodega_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.ofNullable((String) rows.get(0).get("nombre"));
    }
}

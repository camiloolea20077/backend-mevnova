package com.cloud_tecnological.mednova.repositories.kardex;

import com.cloud_tecnological.mednova.dto.kardex.ExpirationAlertDto;
import com.cloud_tecnological.mednova.dto.kardex.ExpirationAlertFilterParams;
import com.cloud_tecnological.mednova.dto.kardex.KardexFilterParams;
import com.cloud_tecnological.mednova.dto.kardex.KardexItemDto;
import com.cloud_tecnological.mednova.dto.kardex.LowStockAlertDto;
import com.cloud_tecnological.mednova.dto.kardex.LowStockAlertFilterParams;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public class KardexQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public KardexQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-078: Kardex (movimientos cronológicos con saldo acumulado) ─

    public PageImpl<KardexItemDto> listKardex(PageableDto<KardexFilterParams> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        KardexFilterParams filter = pageable.getParams();

        // El saldo acumulado se calcula sobre todos los movimientos del lote
        // (no solo la página actual). Ordenamos por fecha y id ASC para construir
        // el running balance, luego envolvemos en una subconsulta para paginar
        // ordenando DESC.
        StringBuilder inner = new StringBuilder("""
                SELECT m.id                                   AS movimiento_id,
                       m.fecha_movimiento,
                       m.tipo_movimiento,
                       m.lote_id,
                       l.numero_lote,
                       l.fecha_vencimiento,
                       m.servicio_salud_id,
                       s.codigo_interno                       AS servicio_codigo,
                       s.nombre                               AS servicio_nombre,
                       m.bodega_origen_id,
                       bo.nombre                              AS bodega_origen_nombre,
                       m.bodega_destino_id,
                       bd.nombre                              AS bodega_destino_nombre,
                       m.cantidad,
                       m.valor_unitario,
                       m.valor_total,
                       m.referencia_tipo,
                       m.referencia_id,
                       m.motivo,
                       CASE
                           WHEN m.tipo_movimiento IN (
                               'ENTRADA_COMPRA','TRASLADO_ENTRADA',
                               'AJUSTE_POSITIVO','DEVOLUCION_PACIENTE')
                               THEN m.cantidad
                           ELSE -m.cantidad
                       END                                    AS cantidad_signed,
                       SUM(
                           CASE
                               WHEN m.tipo_movimiento IN (
                                   'ENTRADA_COMPRA','TRASLADO_ENTRADA',
                                   'AJUSTE_POSITIVO','DEVOLUCION_PACIENTE')
                                   THEN m.cantidad
                               ELSE -m.cantidad
                           END
                       ) OVER (PARTITION BY m.lote_id
                               ORDER BY m.fecha_movimiento ASC, m.id ASC
                               ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)
                                                              AS saldo_acumulado
                FROM movimiento_inventario m
                INNER JOIN lote           l  ON l.id  = m.lote_id
                INNER JOIN servicio_salud s  ON s.id  = m.servicio_salud_id
                LEFT  JOIN bodega         bo ON bo.id = m.bodega_origen_id
                LEFT  JOIN bodega         bd ON bd.id = m.bodega_destino_id
                WHERE m.empresa_id = :empresa_id
                  AND m.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (filter != null) {
            if (filter.getBatchId() != null) {
                inner.append(" AND m.lote_id = :lote_id ");
                params.addValue("lote_id", filter.getBatchId());
            }
            if (filter.getHealthServiceId() != null) {
                inner.append(" AND m.servicio_salud_id = :servicio_id ");
                params.addValue("servicio_id", filter.getHealthServiceId());
            }
            if (filter.getWarehouseId() != null) {
                inner.append("""
                        AND ( m.bodega_origen_id  = :bodega_id
                           OR m.bodega_destino_id = :bodega_id )
                        """);
                params.addValue("bodega_id", filter.getWarehouseId());
            }
            if (filter.getMovementType() != null && !filter.getMovementType().isBlank()) {
                inner.append(" AND m.tipo_movimiento = :tipo_movimiento ");
                params.addValue("tipo_movimiento", filter.getMovementType());
            }
            if (filter.getFromDate() != null) {
                inner.append(" AND m.fecha_movimiento >= :from_date ");
                params.addValue("from_date", filter.getFromDate());
            }
            if (filter.getToDate() != null) {
                inner.append(" AND m.fecha_movimiento < (:to_date::date + INTERVAL '1 day') ");
                params.addValue("to_date", filter.getToDate());
            }
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "fecha_movimiento";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";

        String sql = "SELECT k.*, COUNT(*) OVER() AS total_rows FROM ("
                + inner
                + ") k ORDER BY k." + orderBy + " " + order
                + ", k.movimiento_id " + order
                + " OFFSET :offset LIMIT :limit";

        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql, params, new ColumnMapRowMapper());
        List<KardexItemDto> dtos = result.stream().map(this::mapRowToKardexDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── HU-FASE2-078: Alertas de vencimiento ────────────────────────────────

    public PageImpl<ExpirationAlertDto> listExpirationAlerts(PageableDto<ExpirationAlertFilterParams> pageable,
                                                             Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        ExpirationAlertFilterParams filter = pageable.getParams();

        boolean expired = filter != null && Boolean.TRUE.equals(filter.getExpired());

        StringBuilder sql = new StringBuilder("""
                SELECT sl.id                                  AS stock_id,
                       sl.lote_id,
                       l.numero_lote,
                       l.fecha_vencimiento,
                       (l.fecha_vencimiento - CURRENT_DATE)   AS dias_vencimiento,
                       (l.fecha_vencimiento < CURRENT_DATE)   AS vencido,
                       l.servicio_salud_id,
                       s.codigo_interno                       AS servicio_codigo,
                       s.nombre                               AS servicio_nombre,
                       sl.bodega_id,
                       b.nombre                               AS bodega_nombre,
                       sl.sede_id,
                       se.nombre                              AS sede_nombre,
                       sl.cantidad_disponible,
                       sl.cantidad_reservada,
                       sl.cantidad_total,
                       COUNT(*) OVER()                        AS total_rows
                FROM stock_lote sl
                INNER JOIN bodega         b  ON b.id  = sl.bodega_id
                INNER JOIN sede           se ON se.id = sl.sede_id
                INNER JOIN lote           l  ON l.id  = sl.lote_id
                INNER JOIN servicio_salud s  ON s.id  = l.servicio_salud_id
                WHERE sl.empresa_id   = :empresa_id
                  AND sl.deleted_at  IS NULL
                  AND l.deleted_at   IS NULL
                  AND sl.cantidad_total > 0
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (expired) {
            sql.append(" AND l.fecha_vencimiento < CURRENT_DATE ");
        } else {
            int days = filter != null && filter.getDaysThreshold() != null && filter.getDaysThreshold() > 0
                    ? filter.getDaysThreshold()
                    : 30;
            sql.append("""
                    AND l.fecha_vencimiento >= CURRENT_DATE
                    AND l.fecha_vencimiento <= CURRENT_DATE + (:dias || ' days')::interval
                    """);
            params.addValue("dias", days);
        }

        if (filter != null) {
            if (filter.getWarehouseId() != null) {
                sql.append(" AND sl.bodega_id = :bodega_id ");
                params.addValue("bodega_id", filter.getWarehouseId());
            }
            if (filter.getBranchId() != null) {
                sql.append(" AND sl.sede_id = :sede_id ");
                params.addValue("sede_id", filter.getBranchId());
            }
            if (filter.getHealthServiceId() != null) {
                sql.append(" AND l.servicio_salud_id = :servicio_id ");
                params.addValue("servicio_id", filter.getHealthServiceId());
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(s.nombre)            LIKE UPPER(:search)
                        OR UPPER(s.codigo_interno) LIKE UPPER(:search)
                        OR UPPER(l.numero_lote)    LIKE UPPER(:search)
                        OR UPPER(b.nombre)         LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "l.fecha_vencimiento";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<ExpirationAlertDto> dtos = result.stream().map(this::mapRowToExpirationAlertDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── HU-FASE2-078: Alertas de stock mínimo ───────────────────────────────

    public PageImpl<LowStockAlertDto> listLowStockAlerts(PageableDto<LowStockAlertFilterParams> pageable,
                                                        Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        LowStockAlertFilterParams filter = pageable.getParams();

        // Umbral por defecto: 10 unidades (stock_minimo no existe en BD).
        BigDecimal threshold = filter != null && filter.getMinimumQuantity() != null
                ? filter.getMinimumQuantity()
                : new BigDecimal("10");

        StringBuilder sql = new StringBuilder("""
                SELECT  agg.servicio_salud_id,
                        s.codigo_interno         AS servicio_codigo,
                        s.nombre                 AS servicio_nombre,
                        agg.bodega_id,
                        b.nombre                 AS bodega_nombre,
                        agg.sede_id,
                        se.nombre                AS sede_nombre,
                        agg.cantidad_disponible,
                        agg.cantidad_reservada,
                        agg.cantidad_total,
                        :minimum                 AS minimo,
                        COUNT(*) OVER()          AS total_rows
                FROM (
                    SELECT  l.servicio_salud_id,
                            sl.bodega_id,
                            sl.sede_id,
                            SUM(sl.cantidad_disponible) AS cantidad_disponible,
                            SUM(sl.cantidad_reservada)  AS cantidad_reservada,
                            SUM(sl.cantidad_total)      AS cantidad_total
                    FROM stock_lote sl
                    INNER JOIN lote l ON l.id = sl.lote_id
                    WHERE sl.empresa_id  = :empresa_id
                      AND sl.deleted_at IS NULL
                      AND l.deleted_at  IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("minimum", threshold);

        if (filter != null) {
            if (filter.getWarehouseId() != null) {
                sql.append(" AND sl.bodega_id = :bodega_id ");
                params.addValue("bodega_id", filter.getWarehouseId());
            }
            if (filter.getBranchId() != null) {
                sql.append(" AND sl.sede_id = :sede_id ");
                params.addValue("sede_id", filter.getBranchId());
            }
            if (filter.getHealthServiceId() != null) {
                sql.append(" AND l.servicio_salud_id = :servicio_id ");
                params.addValue("servicio_id", filter.getHealthServiceId());
            }
        }

        sql.append("""
                    GROUP BY l.servicio_salud_id, sl.bodega_id, sl.sede_id
                ) agg
                INNER JOIN servicio_salud s  ON s.id  = agg.servicio_salud_id
                INNER JOIN bodega         b  ON b.id  = agg.bodega_id
                INNER JOIN sede           se ON se.id = agg.sede_id
                WHERE agg.cantidad_total <= :minimum
                """);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(s.nombre)            LIKE UPPER(:search)
                        OR UPPER(s.codigo_interno) LIKE UPPER(:search)
                        OR UPPER(b.nombre)         LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "agg.cantidad_total";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<LowStockAlertDto> dtos = result.stream().map(this::mapRowToLowStockDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private KardexItemDto mapRowToKardexDto(Map<String, Object> row) {
        return KardexItemDto.builder()
                .movementId(toLong(row.get("movimiento_id")))
                .movementDate(toLocalDateTime(row.get("fecha_movimiento")))
                .movementType((String) row.get("tipo_movimiento"))
                .batchId(toLong(row.get("lote_id")))
                .batchNumber((String) row.get("numero_lote"))
                .expirationDate(toLocalDate(row.get("fecha_vencimiento")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .sourceWarehouseId(toLong(row.get("bodega_origen_id")))
                .sourceWarehouseName((String) row.get("bodega_origen_nombre"))
                .destinationWarehouseId(toLong(row.get("bodega_destino_id")))
                .destinationWarehouseName((String) row.get("bodega_destino_nombre"))
                .quantity(toBigDecimal(row.get("cantidad")))
                .signedQuantity(toBigDecimal(row.get("cantidad_signed")))
                .runningBalance(toBigDecimal(row.get("saldo_acumulado")))
                .unitValue(toBigDecimal(row.get("valor_unitario")))
                .totalValue(toBigDecimal(row.get("valor_total")))
                .referenceType((String) row.get("referencia_tipo"))
                .referenceId(toLong(row.get("referencia_id")))
                .reason((String) row.get("motivo"))
                .build();
    }

    private ExpirationAlertDto mapRowToExpirationAlertDto(Map<String, Object> row) {
        return ExpirationAlertDto.builder()
                .stockId(toLong(row.get("stock_id")))
                .batchId(toLong(row.get("lote_id")))
                .batchNumber((String) row.get("numero_lote"))
                .expirationDate(toLocalDate(row.get("fecha_vencimiento")))
                .daysUntilExpiration(toInteger(row.get("dias_vencimiento")))
                .expired((Boolean) row.get("vencido"))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .warehouseId(toLong(row.get("bodega_id")))
                .warehouseName((String) row.get("bodega_nombre"))
                .branchId(toLong(row.get("sede_id")))
                .branchName((String) row.get("sede_nombre"))
                .availableQuantity(toBigDecimal(row.get("cantidad_disponible")))
                .reservedQuantity(toBigDecimal(row.get("cantidad_reservada")))
                .totalQuantity(toBigDecimal(row.get("cantidad_total")))
                .build();
    }

    private LowStockAlertDto mapRowToLowStockDto(Map<String, Object> row) {
        return LowStockAlertDto.builder()
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .warehouseId(toLong(row.get("bodega_id")))
                .warehouseName((String) row.get("bodega_nombre"))
                .branchId(toLong(row.get("sede_id")))
                .branchName((String) row.get("sede_nombre"))
                .availableQuantity(toBigDecimal(row.get("cantidad_disponible")))
                .reservedQuantity(toBigDecimal(row.get("cantidad_reservada")))
                .totalQuantity(toBigDecimal(row.get("cantidad_total")))
                .minimumQuantity(toBigDecimal(row.get("minimo")))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        return ((Number) value).intValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

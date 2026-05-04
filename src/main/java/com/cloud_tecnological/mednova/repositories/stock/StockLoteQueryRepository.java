package com.cloud_tecnological.mednova.repositories.stock;

import com.cloud_tecnological.mednova.dto.stock.StockFilterParams;
import com.cloud_tecnological.mednova.dto.stock.StockResponseDto;
import com.cloud_tecnological.mednova.dto.stock.StockTableDto;
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
import java.util.Optional;

@Repository
public class StockLoteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public StockLoteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-071: Consulta de stock por lote y bodega ───────────────────

    public Optional<StockResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT sl.id,
                       sl.bodega_id,
                       b.codigo                AS bodega_codigo,
                       b.nombre                AS bodega_nombre,
                       sl.sede_id,
                       se.nombre               AS sede_nombre,
                       sl.lote_id,
                       l.numero_lote           AS lote_numero,
                       l.fecha_vencimiento     AS lote_vencimiento,
                       (l.fecha_vencimiento - CURRENT_DATE) AS dias_vencimiento,
                       (l.fecha_vencimiento < CURRENT_DATE) AS vencido,
                       l.servicio_salud_id,
                       s.codigo_interno        AS servicio_codigo,
                       s.nombre                AS servicio_nombre,
                       sl.cantidad_disponible,
                       sl.cantidad_reservada,
                       sl.cantidad_total,
                       sl.ultimo_movimiento_at,
                       sl.activo
                FROM stock_lote sl
                INNER JOIN bodega         b  ON b.id = sl.bodega_id
                INNER JOIN sede           se ON se.id = sl.sede_id
                INNER JOIN lote           l  ON l.id = sl.lote_id
                INNER JOIN servicio_salud s  ON s.id = l.servicio_salud_id
                WHERE sl.id         = :id
                  AND sl.empresa_id = :empresa_id
                  AND sl.sede_id    = :sede_id
                  AND sl.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public PageImpl<StockTableDto> listStock(PageableDto<StockFilterParams> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        StockFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT sl.id,
                       b.nombre              AS bodega_nombre,
                       se.nombre             AS sede_nombre,
                       s.codigo_interno      AS servicio_codigo,
                       s.nombre              AS servicio_nombre,
                       l.numero_lote         AS lote_numero,
                       l.fecha_vencimiento   AS lote_vencimiento,
                       (l.fecha_vencimiento - CURRENT_DATE) AS dias_vencimiento,
                       (l.fecha_vencimiento < CURRENT_DATE) AS vencido,
                       sl.cantidad_disponible,
                       sl.cantidad_reservada,
                       sl.cantidad_total,
                       COUNT(*) OVER()       AS total_rows
                FROM stock_lote sl
                INNER JOIN bodega         b  ON b.id = sl.bodega_id
                INNER JOIN sede           se ON se.id = sl.sede_id
                INNER JOIN lote           l  ON l.id = sl.lote_id
                INNER JOIN servicio_salud s  ON s.id = l.servicio_salud_id
                WHERE sl.empresa_id = :empresa_id
                  AND sl.sede_id    = :sede_id
                  AND sl.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        // Por defecto solo mostramos filas con stock total > 0 (regla negocio FEFO).
        boolean includeEmpty = filter != null && Boolean.TRUE.equals(filter.getIncludeEmpty());
        if (!includeEmpty) {
            sql.append(" AND sl.cantidad_total > 0 ");
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(s.nombre)        LIKE UPPER(:search)
                        OR UPPER(s.codigo_interno) LIKE UPPER(:search)
                        OR UPPER(l.numero_lote)    LIKE UPPER(:search)
                        OR UPPER(b.nombre)         LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        if (filter != null) {
            if (filter.getWarehouseId() != null) {
                sql.append(" AND sl.bodega_id = :bodega_id ");
                params.addValue("bodega_id", filter.getWarehouseId());
            }
            if (filter.getHealthServiceId() != null) {
                sql.append(" AND l.servicio_salud_id = :servicio_id ");
                params.addValue("servicio_id", filter.getHealthServiceId());
            }
            if (filter.getBatchId() != null) {
                sql.append(" AND sl.lote_id = :lote_id ");
                params.addValue("lote_id", filter.getBatchId());
            }
            if (filter.getExpiringInDays() != null && filter.getExpiringInDays() > 0) {
                sql.append("""
                        AND l.fecha_vencimiento >= CURRENT_DATE
                        AND l.fecha_vencimiento <= CURRENT_DATE + (:dias || ' days')::interval
                        """);
                params.addValue("dias", filter.getExpiringInDays());
            }
        }

        // Orden FEFO por defecto: vencimiento ASC.
        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "l.fecha_vencimiento";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<StockTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private StockResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return StockResponseDto.builder()
                .id(toLong(row.get("id")))
                .warehouseId(toLong(row.get("bodega_id")))
                .warehouseCode((String) row.get("bodega_codigo"))
                .warehouseName((String) row.get("bodega_nombre"))
                .branchId(toLong(row.get("sede_id")))
                .branchName((String) row.get("sede_nombre"))
                .batchId(toLong(row.get("lote_id")))
                .batchNumber((String) row.get("lote_numero"))
                .expirationDate(toLocalDate(row.get("lote_vencimiento")))
                .daysUntilExpiration(toInteger(row.get("dias_vencimiento")))
                .expired((Boolean) row.get("vencido"))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .availableQuantity(toBigDecimal(row.get("cantidad_disponible")))
                .reservedQuantity(toBigDecimal(row.get("cantidad_reservada")))
                .totalQuantity(toBigDecimal(row.get("cantidad_total")))
                .lastMovementAt(toLocalDateTime(row.get("ultimo_movimiento_at")))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private StockTableDto mapRowToTableDto(Map<String, Object> row) {
        return StockTableDto.builder()
                .id(toLong(row.get("id")))
                .warehouseName((String) row.get("bodega_nombre"))
                .branchName((String) row.get("sede_nombre"))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .batchNumber((String) row.get("lote_numero"))
                .expirationDate(toLocalDate(row.get("lote_vencimiento")))
                .daysUntilExpiration(toInteger(row.get("dias_vencimiento")))
                .expired((Boolean) row.get("vencido"))
                .availableQuantity(toBigDecimal(row.get("cantidad_disponible")))
                .reservedQuantity(toBigDecimal(row.get("cantidad_reservada")))
                .totalQuantity(toBigDecimal(row.get("cantidad_total")))
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

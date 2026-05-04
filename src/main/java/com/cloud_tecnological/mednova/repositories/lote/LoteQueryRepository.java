package com.cloud_tecnological.mednova.repositories.lote;

import com.cloud_tecnological.mednova.dto.lote.LoteFilterParams;
import com.cloud_tecnological.mednova.dto.lote.LoteResponseDto;
import com.cloud_tecnological.mednova.dto.lote.LoteTableDto;
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
public class LoteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public LoteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-070: Gestión de lotes y vencimientos ───────────────────────

    public Optional<LoteResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT l.id,
                       l.servicio_salud_id,
                       s.codigo_interno     AS servicio_codigo,
                       s.nombre             AS servicio_nombre,
                       l.numero_lote,
                       l.fecha_fabricacion,
                       l.fecha_vencimiento,
                       (l.fecha_vencimiento - CURRENT_DATE)        AS dias_vencimiento,
                       (l.fecha_vencimiento < CURRENT_DATE)        AS vencido,
                       l.registro_invima,
                       l.proveedor_id,
                       p.codigo             AS proveedor_codigo,
                       t.nombre_completo    AS proveedor_nombre,
                       COALESCE((SELECT SUM(sl.cantidad_total)
                                  FROM stock_lote sl
                                  WHERE sl.lote_id    = l.id
                                    AND sl.empresa_id = l.empresa_id
                                    AND sl.deleted_at IS NULL), 0) AS stock_total,
                       l.observaciones,
                       l.activo,
                       l.created_at
                FROM lote l
                INNER JOIN servicio_salud s ON s.id = l.servicio_salud_id
                LEFT  JOIN proveedor      p ON p.id = l.proveedor_id
                LEFT  JOIN tercero        t ON t.id = p.tercero_id
                WHERE l.id         = :id
                  AND l.empresa_id = :empresa_id
                  AND l.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public boolean hasStock(Long lote_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM stock_lote
                WHERE lote_id    = :lote_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND cantidad_total > 0
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lote_id", lote_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public PageImpl<LoteTableDto> listLotes(PageableDto<LoteFilterParams> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        LoteFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT l.id,
                       s.codigo_interno     AS servicio_codigo,
                       s.nombre             AS servicio_nombre,
                       l.numero_lote,
                       l.fecha_vencimiento,
                       (l.fecha_vencimiento - CURRENT_DATE) AS dias_vencimiento,
                       (l.fecha_vencimiento < CURRENT_DATE) AS vencido,
                       l.registro_invima,
                       t.nombre_completo    AS proveedor_nombre,
                       COALESCE((SELECT SUM(sl.cantidad_total)
                                  FROM stock_lote sl
                                  WHERE sl.lote_id    = l.id
                                    AND sl.empresa_id = l.empresa_id
                                    AND sl.deleted_at IS NULL), 0) AS stock_total,
                       l.activo,
                       COUNT(*) OVER()      AS total_rows
                FROM lote l
                INNER JOIN servicio_salud s ON s.id = l.servicio_salud_id
                LEFT  JOIN proveedor      p ON p.id = l.proveedor_id
                LEFT  JOIN tercero        t ON t.id = p.tercero_id
                WHERE l.empresa_id = :empresa_id
                  AND l.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(l.numero_lote)     LIKE UPPER(:search)
                        OR UPPER(l.registro_invima) LIKE UPPER(:search)
                        OR UPPER(s.nombre)       LIKE UPPER(:search)
                        OR UPPER(s.codigo_interno) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        if (filter != null) {
            if (filter.getHealthServiceId() != null) {
                sql.append(" AND l.servicio_salud_id = :servicio_id ");
                params.addValue("servicio_id", filter.getHealthServiceId());
            }
            if (filter.getSupplierId() != null) {
                sql.append(" AND l.proveedor_id = :proveedor_id ");
                params.addValue("proveedor_id", filter.getSupplierId());
            }
            if (Boolean.TRUE.equals(filter.getExpired())) {
                sql.append(" AND l.fecha_vencimiento < CURRENT_DATE ");
            } else if (filter.getExpiringInDays() != null && filter.getExpiringInDays() > 0) {
                sql.append("""
                        AND l.fecha_vencimiento >= CURRENT_DATE
                        AND l.fecha_vencimiento <= CURRENT_DATE + (:dias || ' days')::interval
                        """);
                params.addValue("dias", filter.getExpiringInDays());
            }
            if (Boolean.TRUE.equals(filter.getOnlyWithStock())) {
                sql.append("""
                         AND EXISTS (
                            SELECT 1 FROM stock_lote sl
                            WHERE sl.lote_id    = l.id
                              AND sl.empresa_id = l.empresa_id
                              AND sl.deleted_at IS NULL
                              AND sl.cantidad_total > 0
                        )
                        """);
            }
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "l.fecha_vencimiento";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<LoteTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private LoteResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return LoteResponseDto.builder()
                .id(toLong(row.get("id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .batchNumber((String) row.get("numero_lote"))
                .manufacturingDate(toLocalDate(row.get("fecha_fabricacion")))
                .expirationDate(toLocalDate(row.get("fecha_vencimiento")))
                .daysUntilExpiration(toInteger(row.get("dias_vencimiento")))
                .expired((Boolean) row.get("vencido"))
                .invimaRegister((String) row.get("registro_invima"))
                .supplierId(toLong(row.get("proveedor_id")))
                .supplierCode((String) row.get("proveedor_codigo"))
                .supplierName((String) row.get("proveedor_nombre"))
                .totalStock(toBigDecimal(row.get("stock_total")))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private LoteTableDto mapRowToTableDto(Map<String, Object> row) {
        return LoteTableDto.builder()
                .id(toLong(row.get("id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .batchNumber((String) row.get("numero_lote"))
                .expirationDate(toLocalDate(row.get("fecha_vencimiento")))
                .daysUntilExpiration(toInteger(row.get("dias_vencimiento")))
                .expired((Boolean) row.get("vencido"))
                .invimaRegister((String) row.get("registro_invima"))
                .supplierName((String) row.get("proveedor_nombre"))
                .totalStock(toBigDecimal(row.get("stock_total")))
                .active((Boolean) row.get("activo"))
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

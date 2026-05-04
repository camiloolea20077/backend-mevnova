package com.cloud_tecnological.mednova.repositories.compra;

import com.cloud_tecnological.mednova.dto.compra.CompraResponseDto;
import com.cloud_tecnological.mednova.dto.compra.CompraTableDto;
import com.cloud_tecnological.mednova.dto.compra.DetalleCompraResponseDto;
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
public class CompraQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CompraQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-069: Recepción de compras ──────────────────────────────────

    public Optional<CompraResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT c.id,
                       c.numero_compra,
                       c.numero_factura_proveedor,
                       c.bodega_id,
                       b.nombre              AS bodega_nombre,
                       c.proveedor_id,
                       p.codigo              AS proveedor_codigo,
                       t.nombre_completo     AS proveedor_nombre,
                       c.fecha_compra,
                       c.fecha_recepcion,
                       c.estado_compra,
                       c.subtotal,
                       c.total_iva,
                       c.total_descuento,
                       c.total,
                       c.soporte_url,
                       c.observaciones,
                       c.activo,
                       c.created_at
                FROM compra c
                INNER JOIN bodega    b ON b.id = c.bodega_id
                INNER JOIN proveedor p ON p.id = c.proveedor_id
                INNER JOIN tercero   t ON t.id = p.tercero_id
                WHERE c.id = :id
                  AND c.empresa_id = :empresa_id
                  AND c.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        CompraResponseDto header = mapRowToResponseDto(rows.get(0));
        header.setItems(findItemsByCompra(id, empresa_id));
        return Optional.of(header);
    }

    public List<DetalleCompraResponseDto> findItemsByCompra(Long compra_id, Long empresa_id) {
        String sql = """
                SELECT d.id,
                       d.servicio_salud_id,
                       s.codigo_interno    AS servicio_codigo,
                       s.nombre            AS servicio_nombre,
                       d.lote_id,
                       l.numero_lote       AS lote_numero,
                       l.fecha_vencimiento AS lote_vencimiento,
                       l.registro_invima   AS lote_invima,
                       d.cantidad,
                       d.valor_unitario,
                       d.porcentaje_iva,
                       d.valor_iva,
                       d.porcentaje_descuento,
                       d.valor_descuento,
                       d.subtotal,
                       d.total,
                       d.observaciones
                FROM detalle_compra d
                INNER JOIN servicio_salud s ON s.id = d.servicio_salud_id
                INNER JOIN lote           l ON l.id = d.lote_id
                WHERE d.compra_id  = :compra_id
                  AND d.empresa_id = :empresa_id
                  AND d.deleted_at IS NULL
                ORDER BY d.id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("compra_id", compra_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream().map(this::mapRowToDetalleDto).toList();
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

    public boolean existsProveedorActivo(Long proveedor_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM proveedor
                WHERE id          = :proveedor_id
                  AND empresa_id  = :empresa_id
                  AND activo      = true
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("proveedor_id", proveedor_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean isServicioMedicamentoOInsumo(Long servicio_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM servicio_salud s
                INNER JOIN categoria_servicio_salud cat ON cat.id = s.categoria_servicio_salud_id
                WHERE s.id          = :servicio_id
                  AND s.empresa_id  = :empresa_id
                  AND s.activo      = true
                  AND s.deleted_at IS NULL
                  AND cat.codigo IN ('MEDICAMENTO','INSUMO')
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("servicio_id", servicio_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public Optional<Long> findLoteIdByEmpresaServicioNumero(Long empresa_id, Long servicio_id, String numero_lote) {
        String sql = """
                SELECT id
                FROM lote
                WHERE empresa_id        = :empresa_id
                  AND servicio_salud_id = :servicio_id
                  AND numero_lote       = :numero_lote
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("servicio_id", servicio_id)
                .addValue("numero_lote", numero_lote);
        List<Long> rows = jdbc.queryForList(sql, params, Long.class);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public Optional<Long> findStockLoteIdByBodegaLote(Long bodega_id, Long lote_id, Long empresa_id) {
        String sql = """
                SELECT id
                FROM stock_lote
                WHERE bodega_id  = :bodega_id
                  AND lote_id    = :lote_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("bodega_id", bodega_id)
                .addValue("lote_id", lote_id)
                .addValue("empresa_id", empresa_id);
        List<Long> rows = jdbc.queryForList(sql, params, Long.class);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public String generateNextNumeroCompra(Long empresa_id) {
        String sql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(numero_compra FROM 'COMP-(\\d+)$') AS integer)), 0) + 1
                FROM compra
                WHERE empresa_id = :empresa_id
                  AND numero_compra ~ '^COMP-\\d+$'
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);
        Integer next = jdbc.queryForObject(sql, params, Integer.class);
        int seq = next == null ? 1 : next;
        return String.format("COMP-%06d", seq);
    }

    public PageImpl<CompraTableDto> listCompras(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT c.id,
                       c.numero_compra,
                       c.numero_factura_proveedor,
                       b.nombre          AS bodega_nombre,
                       t.nombre_completo AS proveedor_nombre,
                       c.fecha_compra,
                       c.fecha_recepcion,
                       c.estado_compra,
                       c.total,
                       c.activo,
                       COUNT(*) OVER()   AS total_rows
                FROM compra c
                INNER JOIN bodega    b ON b.id = c.bodega_id
                INNER JOIN proveedor p ON p.id = c.proveedor_id
                INNER JOIN tercero   t ON t.id = p.tercero_id
                WHERE c.empresa_id = :empresa_id
                  AND c.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(c.numero_compra)              LIKE UPPER(:search)
                        OR UPPER(c.numero_factura_proveedor) LIKE UPPER(:search)
                        OR UPPER(t.nombre_completo)          LIKE UPPER(:search)
                        OR UPPER(b.nombre)                   LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "c.fecha_compra";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<CompraTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private CompraResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return CompraResponseDto.builder()
                .id(toLong(row.get("id")))
                .purchaseNumber((String) row.get("numero_compra"))
                .supplierInvoiceNumber((String) row.get("numero_factura_proveedor"))
                .warehouseId(toLong(row.get("bodega_id")))
                .warehouseName((String) row.get("bodega_nombre"))
                .supplierId(toLong(row.get("proveedor_id")))
                .supplierCode((String) row.get("proveedor_codigo"))
                .supplierName((String) row.get("proveedor_nombre"))
                .purchaseDate(toLocalDate(row.get("fecha_compra")))
                .receptionDate(toLocalDate(row.get("fecha_recepcion")))
                .state((String) row.get("estado_compra"))
                .subtotal(toBigDecimal(row.get("subtotal")))
                .totalVat(toBigDecimal(row.get("total_iva")))
                .totalDiscount(toBigDecimal(row.get("total_descuento")))
                .total(toBigDecimal(row.get("total")))
                .supportUrl((String) row.get("soporte_url"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private DetalleCompraResponseDto mapRowToDetalleDto(Map<String, Object> row) {
        return DetalleCompraResponseDto.builder()
                .id(toLong(row.get("id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .batchId(toLong(row.get("lote_id")))
                .batchNumber((String) row.get("lote_numero"))
                .expirationDate(toLocalDate(row.get("lote_vencimiento")))
                .invimaRegister((String) row.get("lote_invima"))
                .quantity(toBigDecimal(row.get("cantidad")))
                .unitValue(toBigDecimal(row.get("valor_unitario")))
                .vatPercentage(toBigDecimal(row.get("porcentaje_iva")))
                .vatValue(toBigDecimal(row.get("valor_iva")))
                .discountPercentage(toBigDecimal(row.get("porcentaje_descuento")))
                .discountValue(toBigDecimal(row.get("valor_descuento")))
                .subtotal(toBigDecimal(row.get("subtotal")))
                .total(toBigDecimal(row.get("total")))
                .observations((String) row.get("observaciones"))
                .build();
    }

    private CompraTableDto mapRowToTableDto(Map<String, Object> row) {
        return CompraTableDto.builder()
                .id(toLong(row.get("id")))
                .purchaseNumber((String) row.get("numero_compra"))
                .supplierInvoiceNumber((String) row.get("numero_factura_proveedor"))
                .warehouseName((String) row.get("bodega_nombre"))
                .supplierName((String) row.get("proveedor_nombre"))
                .purchaseDate(toLocalDate(row.get("fecha_compra")))
                .receptionDate(toLocalDate(row.get("fecha_recepcion")))
                .state((String) row.get("estado_compra"))
                .total(toBigDecimal(row.get("total")))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
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

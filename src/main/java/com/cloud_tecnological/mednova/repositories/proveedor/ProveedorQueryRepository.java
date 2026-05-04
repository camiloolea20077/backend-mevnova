package com.cloud_tecnological.mednova.repositories.proveedor;

import com.cloud_tecnological.mednova.dto.proveedor.ProveedorResponseDto;
import com.cloud_tecnological.mednova.dto.proveedor.ProveedorTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProveedorQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ProveedorQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-068: Gestión de proveedores ────────────────────────────────

    public Optional<ProveedorResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT p.id,
                       p.tercero_id,
                       t.numero_documento     AS tercero_numero_documento,
                       t.nombre_completo      AS tercero_nombre,
                       p.codigo,
                       p.cuenta_contable,
                       p.plazo_pago_dias,
                       p.descuento_pronto_pago,
                       p.requiere_orden_compra,
                       p.observaciones,
                       p.activo,
                       p.created_at
                FROM proveedor p
                INNER JOIN tercero t ON t.id = p.tercero_id
                WHERE p.id = :id
                  AND p.empresa_id = :empresa_id
                  AND p.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public boolean existsByCodigo(String codigo, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM proveedor
                WHERE codigo = :codigo
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id <> :exclude_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("codigo", codigo)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1L : excludeId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsByTercero(Long tercero_id, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM proveedor
                WHERE tercero_id = :tercero_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id <> :exclude_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1L : excludeId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsTerceroProveedorByEmpresa(Long tercero_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM tercero t
                INNER JOIN tipo_tercero tt ON tt.id = t.tipo_tercero_id
                WHERE t.id = :tercero_id
                  AND t.empresa_id = :empresa_id
                  AND t.deleted_at IS NULL
                  AND tt.codigo = 'PROVEEDOR'
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public PageImpl<ProveedorTableDto> listProveedores(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT p.id,
                       p.codigo,
                       t.numero_documento  AS tercero_numero_documento,
                       t.nombre_completo   AS tercero_nombre,
                       p.plazo_pago_dias,
                       p.descuento_pronto_pago,
                       p.requiere_orden_compra,
                       p.activo,
                       COUNT(*) OVER()     AS total_rows
                FROM proveedor p
                INNER JOIN tercero t ON t.id = p.tercero_id
                WHERE p.empresa_id = :empresa_id
                  AND p.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(p.codigo)            LIKE UPPER(:search)
                        OR UPPER(t.nombre_completo) LIKE UPPER(:search)
                        OR t.numero_documento       = :search_exact
                    )
                    """);
            params.addValue("search", "%" + search + "%");
            params.addValue("search_exact", search);
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "p.codigo";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<ProveedorTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private ProveedorResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return ProveedorResponseDto.builder()
                .id(toLong(row.get("id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .thirdPartyDocumentNumber((String) row.get("tercero_numero_documento"))
                .thirdPartyName((String) row.get("tercero_nombre"))
                .code((String) row.get("codigo"))
                .accountingAccount((String) row.get("cuenta_contable"))
                .paymentTermDays(toInteger(row.get("plazo_pago_dias")))
                .earlyPaymentDiscount(toBigDecimal(row.get("descuento_pronto_pago")))
                .requiresPurchaseOrder((Boolean) row.get("requiere_orden_compra"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private ProveedorTableDto mapRowToTableDto(Map<String, Object> row) {
        return ProveedorTableDto.builder()
                .id(toLong(row.get("id")))
                .code((String) row.get("codigo"))
                .thirdPartyDocumentNumber((String) row.get("tercero_numero_documento"))
                .thirdPartyName((String) row.get("tercero_nombre"))
                .paymentTermDays(toInteger(row.get("plazo_pago_dias")))
                .earlyPaymentDiscount(toBigDecimal(row.get("descuento_pronto_pago")))
                .requiresPurchaseOrder((Boolean) row.get("requiere_orden_compra"))
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

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

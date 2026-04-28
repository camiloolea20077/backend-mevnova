package com.cloud_tecnological.mednova.repositories.detallefactura;

import com.cloud_tecnological.mednova.dto.invoiceitem.InvoiceItemResponseDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.InvoiceItemTableDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DetalleFacturaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DetalleFacturaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<InvoiceItemResponseDto> findActiveById(Long id, Long empresaId) {
        String sql = """
            SELECT
                df.id,
                df.empresa_id,
                df.factura_id,
                df.servicio_salud_id,
                ss.nombre                AS service_name,
                df.atencion_id,
                df.cantidad,
                df.valor_unitario,
                df.porcentaje_iva,
                df.valor_iva,
                df.valor_descuento,
                df.valor_copago,
                df.valor_cuota_moderadora,
                df.subtotal,
                df.total,
                df.diagnostico_id,
                df.observaciones,
                df.activo,
                df.created_at
            FROM detalle_factura df
            INNER JOIN servicio_salud ss ON ss.id = df.servicio_salud_id
            WHERE df.id = :id
              AND df.empresa_id = :empresa_id
              AND df.activo = true
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<InvoiceItemResponseDto> mapped = MapperRepository.mapListToDtoListNull(rows, InvoiceItemResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public PageImpl<InvoiceItemTableDto> listByFactura(Long facturaId, Long empresaId, PageableDto<?> request) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize = request.getRows() != null ? request.getRows().intValue() : 50;

        String sql = """
            SELECT
                df.id,
                df.servicio_salud_id,
                ss.nombre                AS service_name,
                df.cantidad,
                df.valor_unitario,
                df.porcentaje_iva,
                df.valor_iva,
                df.valor_descuento,
                df.valor_copago,
                df.valor_cuota_moderadora,
                df.subtotal,
                df.total,
                COUNT(*) OVER()          AS total_rows
            FROM detalle_factura df
            INNER JOIN servicio_salud ss ON ss.id = df.servicio_salud_id
            WHERE df.factura_id = :factura_id
              AND df.empresa_id = :empresa_id
              AND df.activo = true
            ORDER BY df.id ASC
            OFFSET :offset LIMIT :limit
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("factura_id", facturaId)
            .addValue("empresa_id", empresaId)
            .addValue("offset", (long) pageNumber * pageSize)
            .addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        List<InvoiceItemTableDto> result = MapperRepository.mapListToDtoListNull(rows, InvoiceItemTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();

        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }

    public BigDecimal[] calculateTotals(Long facturaId, Long empresaId) {
        String sql = """
            SELECT
                COALESCE(SUM(subtotal), 0)               AS subtotal,
                COALESCE(SUM(valor_iva), 0)              AS total_iva,
                COALESCE(SUM(valor_descuento), 0)        AS total_descuento,
                COALESCE(SUM(valor_copago), 0)           AS total_copago,
                COALESCE(SUM(valor_cuota_moderadora), 0) AS total_cuota_moderadora,
                COALESCE(SUM(total), 0)                  AS total_neto
            FROM detalle_factura
            WHERE factura_id = :factura_id
              AND empresa_id = :empresa_id
              AND activo = true
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("factura_id", facturaId)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};

        Map<String, Object> row = rows.get(0);
        return new BigDecimal[]{
            toBigDecimal(row.get("subtotal")),
            toBigDecimal(row.get("total_iva")),
            toBigDecimal(row.get("total_descuento")),
            toBigDecimal(row.get("total_copago")),
            toBigDecimal(row.get("total_cuota_moderadora")),
            toBigDecimal(row.get("total_neto"))
        };
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }
}

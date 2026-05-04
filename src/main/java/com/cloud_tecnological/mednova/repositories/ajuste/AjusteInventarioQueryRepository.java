package com.cloud_tecnological.mednova.repositories.ajuste;

import com.cloud_tecnological.mednova.dto.ajuste.AjusteInventarioResponseDto;
import com.cloud_tecnological.mednova.dto.ajuste.AjusteInventarioTableDto;
import com.cloud_tecnological.mednova.dto.ajuste.DetalleAjusteResponseDto;
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
public class AjusteInventarioQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AjusteInventarioQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-077: Ajuste de inventario ─────────────────────────────────────

    public Optional<AjusteInventarioResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT a.id,
                       a.numero_ajuste,
                       a.bodega_id,
                       b.nombre               AS bodega_nombre,
                       a.tipo_ajuste,
                       a.fecha_ajuste,
                       a.motivo,
                       a.valor_total_ajuste,
                       a.estado,
                       a.usuario_creacion,
                       uc.nombre_usuario      AS creador_username,
                       a.aprobado_por_id,
                       ua.nombre_usuario      AS aprobador_username,
                       a.fecha_aprobacion,
                       a.activo,
                       a.created_at
                FROM ajuste_inventario a
                INNER JOIN bodega       b  ON b.id  = a.bodega_id
                LEFT JOIN  usuario      uc ON uc.id = a.usuario_creacion
                LEFT JOIN  usuario      ua ON ua.id = a.aprobado_por_id
                WHERE a.id         = :id
                  AND a.empresa_id = :empresa_id
                  AND a.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        AjusteInventarioResponseDto header = mapRowToResponseDto(rows.get(0));
        header.setItems(findItemsByAjuste(id, empresa_id));
        return Optional.of(header);
    }

    public List<DetalleAjusteResponseDto> findItemsByAjuste(Long ajuste_id, Long empresa_id) {
        String sql = """
                SELECT d.id,
                       d.lote_id,
                       l.numero_lote        AS lote_numero,
                       l.fecha_vencimiento  AS lote_vencimiento,
                       d.servicio_salud_id,
                       s.codigo_interno     AS servicio_codigo,
                       s.nombre             AS servicio_nombre,
                       d.cantidad_sistema,
                       d.cantidad_real,
                       d.diferencia,
                       d.valor_unitario,
                       d.valor_diferencia,
                       d.observaciones
                FROM detalle_ajuste_inventario d
                INNER JOIN lote           l ON l.id = d.lote_id
                INNER JOIN servicio_salud s ON s.id = d.servicio_salud_id
                WHERE d.ajuste_id  = :ajuste_id
                  AND d.empresa_id = :empresa_id
                  AND d.deleted_at IS NULL
                ORDER BY d.id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("ajuste_id", ajuste_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream().map(this::mapRowToDetalleDto).toList();
    }

    public Optional<Map<String, Object>> findBodegaInEmpresa(Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT id,
                       sede_id,
                       nombre
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

    public boolean existsUsuarioActivo(Long usuario_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM usuario
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND activo     = true
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", usuario_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public String generateNextNumeroAjuste(Long empresa_id) {
        String sql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(numero_ajuste FROM 'AJU-(\\d+)$') AS integer)), 0) + 1
                FROM ajuste_inventario
                WHERE empresa_id = :empresa_id
                  AND numero_ajuste ~ '^AJU-\\d+$'
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);
        Integer next = jdbc.queryForObject(sql, params, Integer.class);
        int seq = next == null ? 1 : next;
        return String.format("AJU-%06d", seq);
    }

    public PageImpl<AjusteInventarioTableDto> listAjustes(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT a.id,
                       a.numero_ajuste,
                       b.nombre              AS bodega_nombre,
                       a.tipo_ajuste,
                       a.fecha_ajuste,
                       a.estado,
                       a.valor_total_ajuste,
                       a.activo,
                       COUNT(*) OVER()       AS total_rows
                FROM ajuste_inventario a
                INNER JOIN bodega b ON b.id = a.bodega_id
                WHERE a.empresa_id = :empresa_id
                  AND a.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(a.numero_ajuste) LIKE UPPER(:search)
                        OR UPPER(a.tipo_ajuste) LIKE UPPER(:search)
                        OR UPPER(a.estado)      LIKE UPPER(:search)
                        OR UPPER(b.nombre)      LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "a.fecha_ajuste";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<AjusteInventarioTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private AjusteInventarioResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return AjusteInventarioResponseDto.builder()
                .id(toLong(row.get("id")))
                .adjustmentNumber((String) row.get("numero_ajuste"))
                .warehouseId(toLong(row.get("bodega_id")))
                .warehouseName((String) row.get("bodega_nombre"))
                .adjustmentType((String) row.get("tipo_ajuste"))
                .adjustmentDate(toLocalDate(row.get("fecha_ajuste")))
                .reason((String) row.get("motivo"))
                .totalAdjustmentValue(toBigDecimal(row.get("valor_total_ajuste")))
                .state((String) row.get("estado"))
                .createdById(toLong(row.get("usuario_creacion")))
                .createdByUsername((String) row.get("creador_username"))
                .approvedById(toLong(row.get("aprobado_por_id")))
                .approvedByUsername((String) row.get("aprobador_username"))
                .approvedAt(toLocalDateTime(row.get("fecha_aprobacion")))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private DetalleAjusteResponseDto mapRowToDetalleDto(Map<String, Object> row) {
        return DetalleAjusteResponseDto.builder()
                .id(toLong(row.get("id")))
                .batchId(toLong(row.get("lote_id")))
                .batchNumber((String) row.get("lote_numero"))
                .expirationDate(toLocalDate(row.get("lote_vencimiento")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .systemQuantity(toBigDecimal(row.get("cantidad_sistema")))
                .realQuantity(toBigDecimal(row.get("cantidad_real")))
                .difference(toBigDecimal(row.get("diferencia")))
                .unitValue(toBigDecimal(row.get("valor_unitario")))
                .differenceValue(toBigDecimal(row.get("valor_diferencia")))
                .observations((String) row.get("observaciones"))
                .build();
    }

    private AjusteInventarioTableDto mapRowToTableDto(Map<String, Object> row) {
        return AjusteInventarioTableDto.builder()
                .id(toLong(row.get("id")))
                .adjustmentNumber((String) row.get("numero_ajuste"))
                .warehouseName((String) row.get("bodega_nombre"))
                .adjustmentType((String) row.get("tipo_ajuste"))
                .adjustmentDate(toLocalDate(row.get("fecha_ajuste")))
                .state((String) row.get("estado"))
                .totalAdjustmentValue(toBigDecimal(row.get("valor_total_ajuste")))
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

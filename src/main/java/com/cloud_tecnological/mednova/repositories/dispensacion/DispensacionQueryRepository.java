package com.cloud_tecnological.mednova.repositories.dispensacion;

import com.cloud_tecnological.mednova.dto.dispensacion.DetalleDispensacionResponseDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensacionResponseDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensacionTableDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensationSuggestionItemDto;
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
public class DispensacionQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DispensacionQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-074: Dispensación con trazabilidad de lote ────────────────

    public Optional<DispensacionResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT d.id,
                       d.numero_dispensacion,
                       d.bodega_id,
                       b.nombre                AS bodega_nombre,
                       d.prescripcion_id,
                       pr.numero_prescripcion  AS prescripcion_numero,
                       d.paciente_id,
                       tp.nombre_completo      AS paciente_nombre,
                       d.profesional_dispensador_id,
                       td.nombre_completo      AS dispensador_nombre,
                       d.profesional_receptor_id,
                       tr.nombre_completo      AS receptor_nombre,
                       d.fecha_dispensacion,
                       d.estado,
                       d.observaciones,
                       d.activo,
                       d.created_at
                FROM dispensacion d
                INNER JOIN bodega b              ON b.id  = d.bodega_id
                LEFT  JOIN prescripcion pr       ON pr.id = d.prescripcion_id
                INNER JOIN paciente pac          ON pac.id = d.paciente_id
                LEFT  JOIN tercero tp            ON tp.id  = pac.tercero_id
                INNER JOIN profesional_salud psd ON psd.id = d.profesional_dispensador_id
                LEFT  JOIN tercero td            ON td.id  = psd.tercero_id
                LEFT  JOIN profesional_salud psr ON psr.id = d.profesional_receptor_id
                LEFT  JOIN tercero tr            ON tr.id  = psr.tercero_id
                WHERE d.id         = :id
                  AND d.empresa_id = :empresa_id
                  AND d.sede_id    = :sede_id
                  AND d.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        DispensacionResponseDto header = mapRowToResponseDto(rows.get(0));
        header.setItems(findItemsByDispensacion(id, empresa_id));
        return Optional.of(header);
    }

    public List<DetalleDispensacionResponseDto> findItemsByDispensacion(Long dispensacion_id, Long empresa_id) {
        String sql = """
                SELECT dd.id,
                       dd.detalle_prescripcion_id,
                       dd.servicio_salud_id,
                       sv.codigo_interno    AS servicio_codigo,
                       sv.nombre            AS servicio_nombre,
                       dd.lote_id,
                       l.numero_lote,
                       l.fecha_vencimiento,
                       dd.cantidad,
                       dd.valor_unitario,
                       dd.observaciones
                FROM detalle_dispensacion dd
                INNER JOIN servicio_salud sv ON sv.id = dd.servicio_salud_id
                INNER JOIN lote l            ON l.id  = dd.lote_id
                WHERE dd.dispensacion_id = :dispensacion_id
                  AND dd.empresa_id      = :empresa_id
                  AND dd.deleted_at IS NULL
                ORDER BY dd.id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("dispensacion_id", dispensacion_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream().map(this::mapRowToDetalleDto).toList();
    }

    public boolean existsBodegaActivaPermiteDispensar(Long bodega_id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT COUNT(*)
                FROM bodega
                WHERE id          = :bodega_id
                  AND empresa_id  = :empresa_id
                  AND sede_id     = :sede_id
                  AND activo      = true
                  AND permite_dispensar = true
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("bodega_id", bodega_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsProfesionalByEmpresa(Long profesional_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM profesional_salud
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", profesional_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    /**
     * Retorna datos de la prescripción solo si está ACTIVA y pertenece a la sede del tenant.
     * Mapa con: id, paciente_id, numero_prescripcion. Vacío si no califica.
     */
    public Optional<Map<String, Object>> findActivePrescripcion(Long prescripcion_id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT pr.id,
                       pr.numero_prescripcion,
                       ad.paciente_id
                FROM prescripcion pr
                INNER JOIN estado_prescripcion ep ON ep.id = pr.estado_prescripcion_id
                INNER JOIN atencion a             ON a.id  = pr.atencion_id
                INNER JOIN admision ad            ON ad.id = a.admision_id
                WHERE pr.id         = :id
                  AND pr.empresa_id = :empresa_id
                  AND pr.sede_id    = :sede_id
                  AND ep.codigo     = 'ACTIVA'
                  AND pr.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", prescripcion_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Detalle de prescripción válido para la prescripción dada. Devuelve servicio_salud_id y cantidad_despachar.
     */
    public Optional<Map<String, Object>> findDetallePrescripcion(Long detalle_id, Long prescripcion_id, Long empresa_id) {
        String sql = """
                SELECT dp.id,
                       dp.servicio_salud_id,
                       dp.cantidad_despachar
                FROM detalle_prescripcion dp
                WHERE dp.id              = :id
                  AND dp.prescripcion_id = :prescripcion_id
                  AND dp.empresa_id      = :empresa_id
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", detalle_id)
                .addValue("prescripcion_id", prescripcion_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Suma la cantidad ya dispensada (no anulada) para un detalle de prescripción.
     */
    public BigDecimal sumDispensedQuantityByDetallePrescripcion(Long detalle_prescripcion_id, Long empresa_id) {
        String sql = """
                SELECT COALESCE(SUM(dd.cantidad), 0) AS total_dispensado
                FROM detalle_dispensacion dd
                INNER JOIN dispensacion d ON d.id = dd.dispensacion_id
                WHERE dd.detalle_prescripcion_id = :detalle_id
                  AND dd.empresa_id              = :empresa_id
                  AND dd.deleted_at IS NULL
                  AND d.estado <> 'ANULADA'
                  AND d.deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("detalle_id", detalle_prescripcion_id)
                .addValue("empresa_id", empresa_id);
        BigDecimal total = jdbc.queryForObject(sql, params, BigDecimal.class);
        return total == null ? BigDecimal.ZERO : total;
    }

    /**
     * Lote sugerido por FEFO en la bodega indicada: el más cercano a vencer,
     * no vencido, con stock disponible para el servicio dado.
     */
    public Optional<Map<String, Object>> findFefoLoteSuggestion(Long servicio_id, Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT l.id              AS lote_id,
                       l.numero_lote,
                       l.fecha_vencimiento,
                       sl.cantidad_disponible
                FROM lote l
                INNER JOIN stock_lote sl ON sl.lote_id = l.id
                WHERE l.empresa_id        = :empresa_id
                  AND l.servicio_salud_id = :servicio_id
                  AND sl.bodega_id        = :bodega_id
                  AND sl.empresa_id       = :empresa_id
                  AND sl.cantidad_disponible > 0
                  AND l.fecha_vencimiento >= CURRENT_DATE
                  AND l.deleted_at IS NULL
                  AND sl.deleted_at IS NULL
                ORDER BY l.fecha_vencimiento ASC, l.id ASC
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("servicio_id", servicio_id)
                .addValue("bodega_id", bodega_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Stock del lote en la bodega indicada: stock_lote_id, cantidad_disponible, fecha_vencimiento, servicio_salud_id.
     */
    public Optional<Map<String, Object>> findStockLoteInBodega(Long lote_id, Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT sl.id              AS stock_lote_id,
                       sl.cantidad_disponible,
                       l.fecha_vencimiento,
                       l.servicio_salud_id,
                       l.numero_lote
                FROM stock_lote sl
                INNER JOIN lote l ON l.id = sl.lote_id
                WHERE sl.lote_id   = :lote_id
                  AND sl.bodega_id = :bodega_id
                  AND sl.empresa_id = :empresa_id
                  AND sl.deleted_at IS NULL
                  AND l.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lote_id", lote_id)
                .addValue("bodega_id", bodega_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public String generateNextNumeroDispensacion(Long empresa_id) {
        String sql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(numero_dispensacion FROM 'DSP-(\\d+)$') AS integer)), 0) + 1
                FROM dispensacion
                WHERE empresa_id = :empresa_id
                  AND numero_dispensacion ~ '^DSP-\\d+$'
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("empresa_id", empresa_id);
        Integer next = jdbc.queryForObject(sql, params, Integer.class);
        int seq = next == null ? 1 : next;
        return String.format("DSP-%06d", seq);
    }

    public PageImpl<DispensacionTableDto> listDispensaciones(PageableDto<?> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT d.id,
                       d.numero_dispensacion,
                       pr.numero_prescripcion AS prescripcion_numero,
                       tp.nombre_completo     AS paciente_nombre,
                       b.nombre               AS bodega_nombre,
                       td.nombre_completo     AS dispensador_nombre,
                       d.fecha_dispensacion,
                       d.estado,
                       d.activo,
                       COUNT(*) OVER()        AS total_rows
                FROM dispensacion d
                INNER JOIN bodega b              ON b.id  = d.bodega_id
                LEFT  JOIN prescripcion pr       ON pr.id = d.prescripcion_id
                INNER JOIN paciente pac          ON pac.id = d.paciente_id
                LEFT  JOIN tercero tp            ON tp.id  = pac.tercero_id
                INNER JOIN profesional_salud psd ON psd.id = d.profesional_dispensador_id
                LEFT  JOIN tercero td            ON td.id  = psd.tercero_id
                WHERE d.empresa_id = :empresa_id
                  AND d.sede_id    = :sede_id
                  AND d.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(d.numero_dispensacion)  LIKE UPPER(:search)
                        OR UPPER(pr.numero_prescripcion) LIKE UPPER(:search)
                        OR UPPER(tp.nombre_completo)     LIKE UPPER(:search)
                        OR UPPER(b.nombre)               LIKE UPPER(:search)
                        OR UPPER(d.estado)               LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by();
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        if (orderBy == null || orderBy.isBlank()) {
            orderBy = "d.fecha_dispensacion";
        } else {
            orderBy = "d." + orderBy;
        }
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<DispensacionTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    /**
     * Sugerencias FEFO por cada detalle de prescripción pendiente de dispensar.
     * Calcula cantidad ya dispensada (no anulada) y cantidad pendiente.
     */
    public List<DispensationSuggestionItemDto> listDispensationSuggestions(Long prescripcion_id, Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT dp.id              AS prescripcion_detalle_id,
                       dp.servicio_salud_id,
                       sv.codigo_interno   AS servicio_codigo,
                       sv.nombre           AS servicio_nombre,
                       COALESCE(dp.cantidad_despachar, 0) AS cantidad_prescrita,
                       COALESCE(disp.total_dispensado, 0) AS total_dispensado,
                       fefo.lote_id              AS sugerido_lote_id,
                       fefo.numero_lote          AS sugerido_lote_numero,
                       fefo.fecha_vencimiento    AS sugerido_lote_vencimiento,
                       fefo.cantidad_disponible  AS sugerido_disponible
                FROM detalle_prescripcion dp
                INNER JOIN prescripcion pr  ON pr.id = dp.prescripcion_id
                INNER JOIN servicio_salud sv ON sv.id = dp.servicio_salud_id
                LEFT JOIN LATERAL (
                    SELECT COALESCE(SUM(dd.cantidad), 0) AS total_dispensado
                    FROM detalle_dispensacion dd
                    INNER JOIN dispensacion d ON d.id = dd.dispensacion_id
                    WHERE dd.detalle_prescripcion_id = dp.id
                      AND dd.empresa_id              = :empresa_id
                      AND dd.deleted_at IS NULL
                      AND d.estado <> 'ANULADA'
                      AND d.deleted_at IS NULL
                ) disp ON TRUE
                LEFT JOIN LATERAL (
                    SELECT l.id            AS lote_id,
                           l.numero_lote,
                           l.fecha_vencimiento,
                           sl.cantidad_disponible
                    FROM lote l
                    INNER JOIN stock_lote sl ON sl.lote_id = l.id
                    WHERE l.empresa_id        = pr.empresa_id
                      AND l.servicio_salud_id = dp.servicio_salud_id
                      AND sl.bodega_id        = :bodega_id
                      AND sl.empresa_id       = pr.empresa_id
                      AND sl.cantidad_disponible > 0
                      AND l.fecha_vencimiento >= CURRENT_DATE
                      AND l.deleted_at IS NULL
                      AND sl.deleted_at IS NULL
                    ORDER BY l.fecha_vencimiento ASC, l.id ASC
                    LIMIT 1
                ) fefo ON TRUE
                WHERE dp.prescripcion_id = :prescripcion_id
                  AND dp.empresa_id      = :empresa_id
                  AND dp.activo          = true
                ORDER BY dp.id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("prescripcion_id", prescripcion_id)
                .addValue("bodega_id", bodega_id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream().map(row -> {
            BigDecimal prescrita    = toBigDecimal(row.get("cantidad_prescrita"));
            BigDecimal dispensada   = toBigDecimal(row.get("total_dispensado"));
            BigDecimal pendiente    = prescrita == null ? BigDecimal.ZERO : prescrita.subtract(dispensada == null ? BigDecimal.ZERO : dispensada);
            return DispensationSuggestionItemDto.builder()
                    .prescriptionDetailId(toLong(row.get("prescripcion_detalle_id")))
                    .healthServiceId(toLong(row.get("servicio_salud_id")))
                    .healthServiceCode((String) row.get("servicio_codigo"))
                    .healthServiceName((String) row.get("servicio_nombre"))
                    .prescribedQuantity(prescrita)
                    .alreadyDispensedQuantity(dispensada)
                    .pendingQuantity(pendiente)
                    .suggestedBatchId(toLong(row.get("sugerido_lote_id")))
                    .suggestedBatchNumber((String) row.get("sugerido_lote_numero"))
                    .suggestedExpirationDate(toLocalDate(row.get("sugerido_lote_vencimiento")))
                    .suggestedAvailableQuantity(toBigDecimal(row.get("sugerido_disponible")))
                    .canDispense(row.get("sugerido_lote_id") != null)
                    .build();
        }).toList();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private DispensacionResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return DispensacionResponseDto.builder()
                .id(toLong(row.get("id")))
                .dispensationNumber((String) row.get("numero_dispensacion"))
                .warehouseId(toLong(row.get("bodega_id")))
                .warehouseName((String) row.get("bodega_nombre"))
                .prescriptionId(toLong(row.get("prescripcion_id")))
                .prescriptionNumber((String) row.get("prescripcion_numero"))
                .patientId(toLong(row.get("paciente_id")))
                .patientName((String) row.get("paciente_nombre"))
                .dispensingProfessionalId(toLong(row.get("profesional_dispensador_id")))
                .dispensingProfessionalName((String) row.get("dispensador_nombre"))
                .receivingProfessionalId(toLong(row.get("profesional_receptor_id")))
                .receivingProfessionalName((String) row.get("receptor_nombre"))
                .dispensationDate(toLocalDateTime(row.get("fecha_dispensacion")))
                .state((String) row.get("estado"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private DetalleDispensacionResponseDto mapRowToDetalleDto(Map<String, Object> row) {
        return DetalleDispensacionResponseDto.builder()
                .id(toLong(row.get("id")))
                .prescriptionDetailId(toLong(row.get("detalle_prescripcion_id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .batchId(toLong(row.get("lote_id")))
                .batchNumber((String) row.get("numero_lote"))
                .batchExpirationDate(toLocalDate(row.get("fecha_vencimiento")))
                .quantity(toBigDecimal(row.get("cantidad")))
                .unitValue(toBigDecimal(row.get("valor_unitario")))
                .observations((String) row.get("observaciones"))
                .build();
    }

    private DispensacionTableDto mapRowToTableDto(Map<String, Object> row) {
        return DispensacionTableDto.builder()
                .id(toLong(row.get("id")))
                .dispensationNumber((String) row.get("numero_dispensacion"))
                .prescriptionNumber((String) row.get("prescripcion_numero"))
                .patientName((String) row.get("paciente_nombre"))
                .warehouseName((String) row.get("bodega_nombre"))
                .dispensingProfessionalName((String) row.get("dispensador_nombre"))
                .dispensationDate(toLocalDateTime(row.get("fecha_dispensacion")))
                .state((String) row.get("estado"))
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

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return null;
    }
}

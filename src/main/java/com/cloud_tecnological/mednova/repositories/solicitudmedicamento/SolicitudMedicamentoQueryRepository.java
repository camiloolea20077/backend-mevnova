package com.cloud_tecnological.mednova.repositories.solicitudmedicamento;

import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DetalleSolicitudMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DispatchSuggestionItemDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.SolicitudMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.SolicitudMedicamentoTableDto;
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
public class SolicitudMedicamentoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SolicitudMedicamentoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-072: Solicitud de medicamento desde servicio ───────────────

    public Optional<SolicitudMedicamentoResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT s.id,
                       s.numero_solicitud,
                       s.bodega_origen_id,
                       bo.nombre              AS bodega_origen_nombre,
                       s.bodega_destino_id,
                       bd.nombre              AS bodega_destino_nombre,
                       s.profesional_solicitante_id,
                       t.nombre_completo      AS profesional_nombre,
                       s.estado_solicitud,
                       s.prioridad,
                       s.fecha_solicitud,
                       s.fecha_despacho,
                       s.motivo,
                       s.observaciones,
                       s.activo,
                       s.created_at
                FROM solicitud_medicamento s
                INNER JOIN bodega bo ON bo.id = s.bodega_origen_id
                INNER JOIN bodega bd ON bd.id = s.bodega_destino_id
                LEFT  JOIN profesional_salud p ON p.id = s.profesional_solicitante_id
                LEFT  JOIN tercero t ON t.id = p.tercero_id
                WHERE s.id         = :id
                  AND s.empresa_id = :empresa_id
                  AND s.sede_id    = :sede_id
                  AND s.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        SolicitudMedicamentoResponseDto header = mapRowToResponseDto(rows.get(0));
        header.setItems(findItemsBySolicitud(id, empresa_id));
        return Optional.of(header);
    }

    public List<DetalleSolicitudMedicamentoResponseDto> findItemsBySolicitud(Long solicitud_id, Long empresa_id) {
        String sql = """
                SELECT d.id,
                       d.servicio_salud_id,
                       sv.codigo_interno    AS servicio_codigo,
                       sv.nombre            AS servicio_nombre,
                       d.cantidad_solicitada,
                       d.cantidad_despachada,
                       d.estado,
                       d.motivo_rechazo
                FROM detalle_solicitud_medicamento d
                INNER JOIN servicio_salud sv ON sv.id = d.servicio_salud_id
                WHERE d.solicitud_id = :solicitud_id
                  AND d.empresa_id   = :empresa_id
                  AND d.deleted_at IS NULL
                ORDER BY d.id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("solicitud_id", solicitud_id)
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

    public String generateNextNumeroSolicitud(Long empresa_id) {
        String sql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(numero_solicitud FROM 'SOL-(\\d+)$') AS integer)), 0) + 1
                FROM solicitud_medicamento
                WHERE empresa_id = :empresa_id
                  AND numero_solicitud ~ '^SOL-\\d+$'
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);
        Integer next = jdbc.queryForObject(sql, params, Integer.class);
        int seq = next == null ? 1 : next;
        return String.format("SOL-%06d", seq);
    }

    public PageImpl<SolicitudMedicamentoTableDto> listSolicitudes(PageableDto<?> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.numero_solicitud,
                       bo.nombre              AS bodega_origen_nombre,
                       bd.nombre              AS bodega_destino_nombre,
                       t.nombre_completo      AS profesional_nombre,
                       s.prioridad,
                       s.estado_solicitud,
                       s.fecha_solicitud,
                       s.activo,
                       COUNT(*) OVER()        AS total_rows
                FROM solicitud_medicamento s
                INNER JOIN bodega bo ON bo.id = s.bodega_origen_id
                INNER JOIN bodega bd ON bd.id = s.bodega_destino_id
                LEFT  JOIN profesional_salud p ON p.id = s.profesional_solicitante_id
                LEFT  JOIN tercero t ON t.id = p.tercero_id
                WHERE s.empresa_id = :empresa_id
                  AND s.sede_id    = :sede_id
                  AND s.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(s.numero_solicitud) LIKE UPPER(:search)
                        OR UPPER(bo.nombre)        LIKE UPPER(:search)
                        OR UPPER(bd.nombre)        LIKE UPPER(:search)
                        OR UPPER(s.estado_solicitud) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        // Orden por defecto: prioridad VITAL → URGENTE → NORMAL, luego fecha_solicitud ASC
        // (regla negocio HU-072: VITAL ordena al inicio del listado de farmacia).
        String orderBy = pageable.getOrder_by();
        String order;
        if (orderBy == null) {
            sql.append("""
                    ORDER BY CASE s.prioridad
                                 WHEN 'VITAL'   THEN 1
                                 WHEN 'URGENTE' THEN 2
                                 WHEN 'NORMAL'  THEN 3
                                 ELSE 4
                             END ASC,
                             s.fecha_solicitud ASC
                    """);
        } else {
            order = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
            sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        }
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<SolicitudMedicamentoTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── HU-FASE2-073: Despacho FEFO ─────────────────────────────────────────

    /**
     * Lote sugerido por FEFO: el más cercano a vencer, no vencido, con stock
     * disponible en la bodega de origen para el servicio dado.
     */
    public Optional<Map<String, Object>> findFefoLoteSuggestion(Long servicio_id, Long bodega_origen_id, Long empresa_id) {
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
                .addValue("bodega_id", bodega_origen_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * Devuelve datos del lote: stock_lote_id en bodega origen, cantidad_disponible
     * y fecha_vencimiento. Empty si el lote no está en esa bodega.
     */
    public Optional<Map<String, Object>> findStockLoteInBodega(Long lote_id, Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT sl.id              AS stock_lote_id,
                       sl.cantidad_disponible,
                       l.fecha_vencimiento,
                       l.servicio_salud_id
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

    /**
     * Devuelve la sugerencia FEFO por cada detalle pendiente de la solicitud.
     */
    public List<DispatchSuggestionItemDto> listDispatchSuggestions(Long solicitud_id, Long empresa_id) {
        String sql = """
                SELECT d.id                AS detalle_id,
                       d.servicio_salud_id,
                       sv.codigo_interno   AS servicio_codigo,
                       sv.nombre           AS servicio_nombre,
                       d.cantidad_solicitada,
                       fefo.lote_id              AS sugerido_lote_id,
                       fefo.numero_lote          AS sugerido_lote_numero,
                       fefo.fecha_vencimiento    AS sugerido_lote_vencimiento,
                       fefo.cantidad_disponible  AS sugerido_disponible
                FROM detalle_solicitud_medicamento d
                INNER JOIN solicitud_medicamento s ON s.id = d.solicitud_id
                INNER JOIN servicio_salud sv       ON sv.id = d.servicio_salud_id
                LEFT JOIN LATERAL (
                    SELECT l.id            AS lote_id,
                           l.numero_lote,
                           l.fecha_vencimiento,
                           sl.cantidad_disponible
                    FROM lote l
                    INNER JOIN stock_lote sl ON sl.lote_id = l.id
                    WHERE l.empresa_id        = s.empresa_id
                      AND l.servicio_salud_id = d.servicio_salud_id
                      AND sl.bodega_id        = s.bodega_origen_id
                      AND sl.empresa_id       = s.empresa_id
                      AND sl.cantidad_disponible > 0
                      AND l.fecha_vencimiento >= CURRENT_DATE
                      AND l.deleted_at IS NULL
                      AND sl.deleted_at IS NULL
                    ORDER BY l.fecha_vencimiento ASC, l.id ASC
                    LIMIT 1
                ) fefo ON TRUE
                WHERE d.solicitud_id = :solicitud_id
                  AND d.empresa_id   = :empresa_id
                  AND d.estado IN ('PENDIENTE','PARCIAL')
                  AND d.deleted_at IS NULL
                ORDER BY d.id ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("solicitud_id", solicitud_id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream().map(row -> DispatchSuggestionItemDto.builder()
                .detailId(toLong(row.get("detalle_id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .requestedQuantity(toBigDecimal(row.get("cantidad_solicitada")))
                .suggestedBatchId(toLong(row.get("sugerido_lote_id")))
                .suggestedBatchNumber((String) row.get("sugerido_lote_numero"))
                .suggestedExpirationDate(toLocalDate(row.get("sugerido_lote_vencimiento")))
                .suggestedAvailableQuantity(toBigDecimal(row.get("sugerido_disponible")))
                .canDispatch(row.get("sugerido_lote_id") != null)
                .build()
        ).toList();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private SolicitudMedicamentoResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return SolicitudMedicamentoResponseDto.builder()
                .id(toLong(row.get("id")))
                .requestNumber((String) row.get("numero_solicitud"))
                .sourceWarehouseId(toLong(row.get("bodega_origen_id")))
                .sourceWarehouseName((String) row.get("bodega_origen_nombre"))
                .destinationWarehouseId(toLong(row.get("bodega_destino_id")))
                .destinationWarehouseName((String) row.get("bodega_destino_nombre"))
                .requestingProfessionalId(toLong(row.get("profesional_solicitante_id")))
                .requestingProfessionalName((String) row.get("profesional_nombre"))
                .state((String) row.get("estado_solicitud"))
                .priority((String) row.get("prioridad"))
                .requestDate(toLocalDateTime(row.get("fecha_solicitud")))
                .dispatchDate(toLocalDateTime(row.get("fecha_despacho")))
                .reason((String) row.get("motivo"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private DetalleSolicitudMedicamentoResponseDto mapRowToDetalleDto(Map<String, Object> row) {
        return DetalleSolicitudMedicamentoResponseDto.builder()
                .id(toLong(row.get("id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceCode((String) row.get("servicio_codigo"))
                .healthServiceName((String) row.get("servicio_nombre"))
                .requestedQuantity(toBigDecimal(row.get("cantidad_solicitada")))
                .dispatchedQuantity(toBigDecimal(row.get("cantidad_despachada")))
                .state((String) row.get("estado"))
                .rejectionReason((String) row.get("motivo_rechazo"))
                .build();
    }

    private SolicitudMedicamentoTableDto mapRowToTableDto(Map<String, Object> row) {
        return SolicitudMedicamentoTableDto.builder()
                .id(toLong(row.get("id")))
                .requestNumber((String) row.get("numero_solicitud"))
                .sourceWarehouseName((String) row.get("bodega_origen_nombre"))
                .destinationWarehouseName((String) row.get("bodega_destino_nombre"))
                .requestingProfessionalName((String) row.get("profesional_nombre"))
                .priority((String) row.get("prioridad"))
                .state((String) row.get("estado_solicitud"))
                .requestDate(toLocalDateTime(row.get("fecha_solicitud")))
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

package com.cloud_tecnological.mednova.repositories.factura;

import com.cloud_tecnological.mednova.dto.invoice.InvoiceResponseDto;
import com.cloud_tecnological.mednova.dto.invoice.InvoiceTableDto;
import com.cloud_tecnological.mednova.dto.invoiceitem.InvoiceItemTableDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FacturaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public FacturaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String generateNumeroFactura(Long empresaId, String prefijo) {
        String sql = """
            SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(numero, '[^0-9]', '', 'g') AS BIGINT)), 0) + 1
            FROM factura
            WHERE empresa_id = :empresa_id
              AND COALESCE(prefijo, '') = COALESCE(:prefijo, '')
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("prefijo", prefijo);
        Long next = jdbc.queryForObject(sql, params, Long.class);
        return String.format("%08d", next);
    }

    public boolean existsFacturaForAdmision(Long admisionId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM factura
            WHERE admision_id = :admision_id
              AND empresa_id = :empresa_id
              AND deleted_at IS NULL
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("admision_id", admisionId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public Long findEstadoFacturaIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_factura WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public String findEstadoFacturaCodigoById(Long estadoId) {
        String sql = "SELECT codigo FROM estado_factura WHERE id = :id LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("id", estadoId), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return (String) rows.get(0).get("codigo");
    }

    public Optional<InvoiceResponseDto> findActiveById(Long id, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                f.id,
                f.empresa_id,
                f.sede_id,
                f.prefijo,
                f.numero,
                f.admision_id,
                a.numero_admision        AS admission_number,
                f.paciente_id,
                t.nombre_completo        AS patient_name,
                t.numero_documento       AS document_number,
                f.pagador_id,
                tp.nombre_completo       AS payer_name,
                f.contrato_id,
                f.estado_factura_id,
                ef.codigo                AS status_code,
                ef.nombre                AS status_name,
                f.fecha_factura,
                f.fecha_vencimiento,
                f.subtotal,
                f.total_iva,
                f.total_descuento,
                f.total_copago,
                f.total_cuota_moderadora,
                f.total_neto,
                f.observaciones,
                f.activo,
                f.created_at
            FROM factura f
            INNER JOIN admision a         ON a.id = f.admision_id
            INNER JOIN paciente p         ON p.id = f.paciente_id
            INNER JOIN tercero t          ON t.id = p.tercero_id AND t.deleted_at IS NULL
            INNER JOIN pagador pag        ON pag.id = f.pagador_id
            INNER JOIN tercero tp         ON tp.id = pag.tercero_id
            INNER JOIN estado_factura ef  ON ef.id = f.estado_factura_id
            WHERE f.id = :id
              AND f.empresa_id = :empresa_id
              AND f.sede_id = :sede_id
              AND f.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<InvoiceResponseDto> mapped = MapperRepository.mapListToDtoListNull(rows, InvoiceResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public PageImpl<InvoiceTableDto> listActive(PageableDto<?> request, Long empresaId, Long sedeId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize = request.getRows() != null ? request.getRows().intValue() : 10;
        String search = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT
                f.id,
                f.numero,
                f.prefijo,
                t.nombre_completo        AS patient_name,
                t.numero_documento       AS document_number,
                tp.nombre_completo       AS payer_name,
                ef.codigo                AS status_code,
                ef.nombre                AS status_name,
                f.fecha_factura,
                f.total_neto,
                f.activo,
                COUNT(*) OVER()          AS total_rows
            FROM factura f
            INNER JOIN paciente p         ON p.id = f.paciente_id
            INNER JOIN tercero t          ON t.id = p.tercero_id AND t.deleted_at IS NULL
            INNER JOIN pagador pag        ON pag.id = f.pagador_id
            INNER JOIN tercero tp         ON tp.id = pag.tercero_id
            INNER JOIN estado_factura ef  ON ef.id = f.estado_factura_id
            WHERE f.empresa_id = :empresa_id
              AND f.sede_id = :sede_id
              AND f.deleted_at IS NULL
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                AND (
                    unaccent(LOWER(t.nombre_completo)) ILIKE unaccent(LOWER(:search))
                    OR t.numero_documento = :exactSearch
                    OR f.numero ILIKE :search
                )
            """);
            params.addValue("search", "%" + search + "%");
            params.addValue("exactSearch", search);
        }

        String orderBy = request.getOrder_by() != null ? request.getOrder_by() : "f.fecha_factura";
        String order = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<InvoiceTableDto> result = MapperRepository.mapListToDtoListNull(rows, InvoiceTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();

        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }

    public Optional<Map<String, Object>> findAdmisionContext(Long admisionId, Long empresaId) {
        String sql = """
            SELECT paciente_id, pagador_id, contrato_id, sede_id
            FROM admision
            WHERE id = :id AND empresa_id = :empresa_id AND deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", admisionId)
            .addValue("empresa_id", empresaId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    // Carga previa de servicios desde las ordenes de la admisión con su tarifa
    public List<Map<String, Object>> preloadServicesFromAdmision(Long admisionId, Long contratoId, Long empresaId) {
        String sql = """
            SELECT
                doc.servicio_salud_id,
                ss.nombre                AS service_name,
                SUM(doc.cantidad)        AS cantidad,
                COALESCE(
                    (SELECT tc.valor FROM tarifa_contrato tc
                     WHERE tc.contrato_id = :contrato_id
                       AND tc.servicio_salud_id = doc.servicio_salud_id
                       AND tc.vigente = true
                       AND tc.activo = true
                       AND tc.deleted_at IS NULL
                     ORDER BY tc.created_at DESC LIMIT 1),
                    (SELECT dt.valor FROM detalle_tarifario dt
                     INNER JOIN contrato c ON c.tarifario_id = dt.tarifario_id AND c.id = :contrato_id
                     WHERE dt.servicio_salud_id = doc.servicio_salud_id
                       AND dt.activo = true
                     LIMIT 1),
                    0
                )                        AS valor_unitario,
                at2.id                   AS atencion_id
            FROM atencion at2
            INNER JOIN orden_clinica oc        ON oc.atencion_id = at2.id AND oc.deleted_at IS NULL
            INNER JOIN detalle_orden_clinica doc ON doc.orden_clinica_id = oc.id AND doc.activo = true
            INNER JOIN servicio_salud ss       ON ss.id = doc.servicio_salud_id AND ss.activo = true
            WHERE at2.admision_id = :admision_id
              AND at2.empresa_id = :empresa_id
              AND at2.deleted_at IS NULL
            GROUP BY doc.servicio_salud_id, ss.nombre, at2.id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("admision_id", admisionId)
            .addValue("contrato_id", contratoId)
            .addValue("empresa_id", empresaId);
        return jdbc.query(sql, params, new ColumnMapRowMapper());
    }
}

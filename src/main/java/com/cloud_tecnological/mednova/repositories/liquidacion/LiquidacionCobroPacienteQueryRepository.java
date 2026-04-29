package com.cloud_tecnological.mednova.repositories.liquidacion;

import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionResponseDto;
import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionSearchRequestDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class LiquidacionCobroPacienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public LiquidacionCobroPacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public java.util.Optional<LiquidacionResponseDto> findActiveById(Long id, Long empresaId) {
        String sql = """
            SELECT
                lcp.id,
                lcp.empresa_id,
                lcp.paciente_id,
                t.nombre_completo        AS patient_name,
                lcp.admision_id,
                lcp.atencion_id,
                lcp.factura_id,
                f.numero                 AS invoice_number,
                lcp.tipo_cobro,
                lcp.servicio_salud_id,
                ss.nombre                AS service_name,
                lcp.regla_cobro_paciente_id,
                lcp.base_calculo,
                lcp.porcentaje_aplicado,
                lcp.valor_calculado,
                lcp.valor_cobrado,
                lcp.aplica_exencion,
                lcp.motivo_exencion,
                lcp.fecha_liquidacion,
                lcp.estado_recaudo,
                lcp.observaciones,
                lcp.activo
            FROM liquidacion_cobro_paciente lcp
            INNER JOIN paciente p          ON p.id = lcp.paciente_id
            INNER JOIN tercero t           ON t.id = p.tercero_id AND t.deleted_at IS NULL
            LEFT JOIN factura f            ON f.id = lcp.factura_id
            LEFT JOIN servicio_salud ss    ON ss.id = lcp.servicio_salud_id
            WHERE lcp.id = :id
              AND lcp.empresa_id = :empresa_id
              AND lcp.activo = true
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return java.util.Optional.empty();
        return java.util.Optional.of(MapperRepository.mapListToDtoListNull(rows, LiquidacionResponseDto.class).get(0));
    }

    public List<LiquidacionResponseDto> findByFactura(Long facturaId, Long empresaId) {
        String sql = """
            SELECT
                lcp.id,
                lcp.empresa_id,
                lcp.paciente_id,
                t.nombre_completo        AS patient_name,
                lcp.admision_id,
                lcp.atencion_id,
                lcp.factura_id,
                f.numero                 AS invoice_number,
                lcp.tipo_cobro,
                lcp.servicio_salud_id,
                ss.nombre                AS service_name,
                lcp.regla_cobro_paciente_id,
                lcp.base_calculo,
                lcp.porcentaje_aplicado,
                lcp.valor_calculado,
                lcp.valor_cobrado,
                lcp.aplica_exencion,
                lcp.motivo_exencion,
                lcp.fecha_liquidacion,
                lcp.estado_recaudo,
                lcp.observaciones,
                lcp.activo
            FROM liquidacion_cobro_paciente lcp
            INNER JOIN paciente p          ON p.id = lcp.paciente_id
            INNER JOIN tercero t           ON t.id = p.tercero_id AND t.deleted_at IS NULL
            LEFT JOIN factura f            ON f.id = lcp.factura_id
            LEFT JOIN servicio_salud ss    ON ss.id = lcp.servicio_salud_id
            WHERE lcp.factura_id = :factura_id
              AND lcp.empresa_id = :empresa_id
              AND lcp.activo = true
            ORDER BY lcp.id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("factura_id", facturaId)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return MapperRepository.mapListToDtoListNull(rows, LiquidacionResponseDto.class);
    }

    public List<LiquidacionResponseDto> findByPaciente(Long pacienteId, Long empresaId) {
        String sql = """
            SELECT
                lcp.id,
                lcp.empresa_id,
                lcp.paciente_id,
                t.nombre_completo        AS patient_name,
                lcp.admision_id,
                lcp.atencion_id,
                lcp.factura_id,
                f.numero                 AS invoice_number,
                lcp.tipo_cobro,
                lcp.servicio_salud_id,
                ss.nombre                AS service_name,
                lcp.regla_cobro_paciente_id,
                lcp.base_calculo,
                lcp.porcentaje_aplicado,
                lcp.valor_calculado,
                lcp.valor_cobrado,
                lcp.aplica_exencion,
                lcp.motivo_exencion,
                lcp.fecha_liquidacion,
                lcp.estado_recaudo,
                lcp.observaciones,
                lcp.activo
            FROM liquidacion_cobro_paciente lcp
            INNER JOIN paciente p          ON p.id = lcp.paciente_id
            INNER JOIN tercero t           ON t.id = p.tercero_id AND t.deleted_at IS NULL
            LEFT JOIN factura f            ON f.id = lcp.factura_id
            LEFT JOIN servicio_salud ss    ON ss.id = lcp.servicio_salud_id
            WHERE lcp.paciente_id = :paciente_id
              AND lcp.empresa_id = :empresa_id
              AND lcp.activo = true
            ORDER BY lcp.fecha_liquidacion DESC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("paciente_id", pacienteId)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return MapperRepository.mapListToDtoListNull(rows, LiquidacionResponseDto.class);
    }

    public PageImpl<LiquidacionResponseDto> search(LiquidacionSearchRequestDto request, Long empresaId) {
        int pageNumber = request.getPage() != null ? request.getPage() : 0;
        int pageSize   = request.getRows() != null ? request.getRows() : 10;

        StringBuilder sql = new StringBuilder("""
            SELECT
                lcp.id,
                lcp.empresa_id,
                lcp.paciente_id,
                t.nombre_completo        AS patient_name,
                lcp.admision_id,
                lcp.atencion_id,
                lcp.factura_id,
                f.numero                 AS invoice_number,
                lcp.tipo_cobro,
                lcp.servicio_salud_id,
                ss.nombre                AS service_name,
                lcp.regla_cobro_paciente_id,
                lcp.base_calculo,
                lcp.porcentaje_aplicado,
                lcp.valor_calculado,
                lcp.valor_cobrado,
                lcp.aplica_exencion,
                lcp.motivo_exencion,
                lcp.fecha_liquidacion,
                lcp.estado_recaudo,
                lcp.observaciones,
                lcp.activo,
                COUNT(*) OVER()          AS total_rows
            FROM liquidacion_cobro_paciente lcp
            INNER JOIN paciente p          ON p.id = lcp.paciente_id
            INNER JOIN tercero t           ON t.id = p.tercero_id AND t.deleted_at IS NULL
            LEFT JOIN factura f            ON f.id = lcp.factura_id
            LEFT JOIN servicio_salud ss    ON ss.id = lcp.servicio_salud_id
            WHERE lcp.empresa_id = :empresa_id
              AND lcp.activo = true
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId);

        if (request.getPacienteId() != null) {
            sql.append(" AND lcp.paciente_id = :paciente_id");
            params.addValue("paciente_id", request.getPacienteId());
        }
        if (request.getFacturaId() != null) {
            sql.append(" AND lcp.factura_id = :factura_id");
            params.addValue("factura_id", request.getFacturaId());
        }
        if (request.getTipoCobro() != null && !request.getTipoCobro().isBlank()) {
            sql.append(" AND lcp.tipo_cobro = :tipo_cobro");
            params.addValue("tipo_cobro", request.getTipoCobro());
        }
        if (request.getEstadoRecaudo() != null && !request.getEstadoRecaudo().isBlank()) {
            sql.append(" AND lcp.estado_recaudo = :estado_recaudo");
            params.addValue("estado_recaudo", request.getEstadoRecaudo());
        }
        if (request.getFechaDesde() != null) {
            sql.append(" AND lcp.fecha_liquidacion >= :fecha_desde");
            params.addValue("fecha_desde", java.sql.Date.valueOf(request.getFechaDesde()));
        }
        if (request.getFechaHasta() != null) {
            sql.append(" AND lcp.fecha_liquidacion <= :fecha_hasta");
            params.addValue("fecha_hasta", java.sql.Date.valueOf(request.getFechaHasta().plusDays(1)));
        }

        sql.append(" ORDER BY lcp.fecha_liquidacion DESC OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<LiquidacionResponseDto> result = MapperRepository.mapListToDtoListNull(rows, LiquidacionResponseDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();

        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }

    // Datos del paciente para el cálculo: regimen_id y sisben actual
    public Map<String, Object> findPatientBillingContext(Long pacienteId, Long empresaId) {
        String sql = """
            SELECT
                ssp.regimen_id,
                COALESCE(sp.grupo_sisben_id, ssp.categoria_afiliacion_id) AS categoria_sisben_id
            FROM paciente p
            LEFT JOIN seguridad_social_paciente ssp
                ON ssp.paciente_id = p.id AND ssp.empresa_id = p.empresa_id
                AND ssp.vigente = true AND ssp.activo = true
                AND ssp.deleted_at IS NULL
            LEFT JOIN sisben_paciente sp
                ON sp.paciente_id = p.id AND sp.empresa_id = p.empresa_id
                AND sp.vigente = true AND sp.activo = true
                AND sp.deleted_at IS NULL
            WHERE p.id = :paciente_id
              AND p.empresa_id = :empresa_id
              AND p.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("paciente_id", pacienteId)
            .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }
}

package com.cloud_tecnological.mednova.repositories.administracionmedicamento;

import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoFilterParams;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoTableDto;
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
public class AdministracionMedicamentoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AdministracionMedicamentoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-087: Lectura por ID ─────────────────────────────────────────

    public Optional<AdministracionMedicamentoResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT a.id,
                       a.atencion_id,
                       a.paciente_id,
                       a.detalle_prescripcion_id,
                       ss.nombre                    AS medicamento_nombre,
                       a.dispensacion_id,
                       d.numero_dispensacion        AS dispensacion_numero,
                       a.lote_id,
                       l.numero_lote                AS lote_numero,
                       a.profesional_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       a.fecha_programada,
                       a.fecha_administracion,
                       a.dosis_administrada,
                       a.via_administracion_id,
                       va.nombre                    AS via_nombre,
                       a.estado,
                       a.motivo_omision,
                       a.reaccion_adversa,
                       a.observaciones,
                       a.activo,
                       a.created_at,
                       a.updated_at,
                       a.usuario_creacion,
                       a.usuario_modificacion
                FROM administracion_medicamento a
                LEFT JOIN detalle_prescripcion dp ON dp.id = a.detalle_prescripcion_id
                LEFT JOIN servicio_salud       ss ON ss.id = dp.servicio_salud_id
                LEFT JOIN dispensacion         d  ON d.id  = a.dispensacion_id
                LEFT JOIN lote                 l  ON l.id  = a.lote_id
                LEFT JOIN profesional_salud    ps ON ps.id = a.profesional_id
                LEFT JOIN tercero              te ON te.id = ps.tercero_id
                LEFT JOIN via_administracion   va ON va.id = a.via_administracion_id
                WHERE a.id         = :id
                  AND a.empresa_id = :empresa_id
                  AND a.sede_id    = :sede_id
                  AND a.deleted_at IS NULL
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

    public PageImpl<AdministracionMedicamentoTableDto> listAdministraciones(
            PageableDto<AdministracionMedicamentoFilterParams> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        AdministracionMedicamentoFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT a.id,
                       a.atencion_id,
                       a.paciente_id,
                       a.detalle_prescripcion_id,
                       ss.nombre                    AS medicamento_nombre,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       a.fecha_programada,
                       a.fecha_administracion,
                       a.dosis_administrada,
                       a.estado,
                       a.reaccion_adversa,
                       a.activo,
                       COUNT(*) OVER()              AS total_rows
                FROM administracion_medicamento a
                LEFT JOIN detalle_prescripcion dp ON dp.id = a.detalle_prescripcion_id
                LEFT JOIN servicio_salud       ss ON ss.id = dp.servicio_salud_id
                LEFT JOIN profesional_salud    ps ON ps.id = a.profesional_id
                LEFT JOIN tercero              te ON te.id = ps.tercero_id
                WHERE a.empresa_id = :empresa_id
                  AND a.sede_id    = :sede_id
                  AND a.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (filter != null) {
            if (filter.getEncounterId() != null) {
                sql.append(" AND a.atencion_id = :atencion_id ");
                params.addValue("atencion_id", filter.getEncounterId());
            }
            if (filter.getPatientId() != null) {
                sql.append(" AND a.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getProfessionalId() != null) {
                sql.append(" AND a.profesional_id = :profesional_id ");
                params.addValue("profesional_id", filter.getProfessionalId());
            }
            if (filter.getPrescriptionDetailId() != null) {
                sql.append(" AND a.detalle_prescripcion_id = :detalle_id ");
                params.addValue("detalle_id", filter.getPrescriptionDetailId());
            }
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                sql.append(" AND a.estado = :estado ");
                params.addValue("estado", filter.getStatus().trim());
            }
            if (Boolean.TRUE.equals(filter.getOnlyPending())) {
                sql.append(" AND a.estado = 'PROGRAMADA' ");
            }
            if (Boolean.TRUE.equals(filter.getOnlyWithAdverseReaction())) {
                sql.append(" AND a.reaccion_adversa IS NOT NULL AND length(trim(a.reaccion_adversa)) > 0 ");
            }
            if (filter.getScheduledFrom() != null) {
                sql.append(" AND a.fecha_programada >= :scheduled_from ");
                params.addValue("scheduled_from", filter.getScheduledFrom().atStartOfDay());
            }
            if (filter.getScheduledTo() != null) {
                sql.append(" AND a.fecha_programada < :scheduled_to ");
                params.addValue("scheduled_to", filter.getScheduledTo().plusDays(1).atStartOfDay());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND a.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(ss.nombre)         LIKE UPPER(:search)
                        OR UPPER(a.observaciones) LIKE UPPER(:search)
                        OR UPPER(a.reaccion_adversa) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "a.fecha_programada";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<AdministracionMedicamentoTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Validaciones cross-tenant ───────────────────────────────────────────

    public boolean atencionExistsInTenant(Long atencion_id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT COUNT(*)
                FROM atencion
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND sede_id    = :sede_id
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", atencion_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id), Long.class);
        return count != null && count > 0;
    }

    public boolean atencionMatchesPaciente(Long atencion_id, Long paciente_id) {
        String sql = """
                SELECT COUNT(*)
                FROM atencion a
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE a.id          = :atencion_id
                  AND adm.paciente_id = :paciente_id
                  AND a.deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("atencion_id", atencion_id)
                .addValue("paciente_id", paciente_id), Long.class);
        return count != null && count > 0;
    }

    public boolean pacienteExistsInEmpresa(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM paciente
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", paciente_id)
                .addValue("empresa_id", empresa_id), Long.class);
        return count != null && count > 0;
    }

    public boolean profesionalActivoInEmpresa(Long profesional_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM profesional_salud
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND activo     = true
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", profesional_id)
                .addValue("empresa_id", empresa_id), Long.class);
        return count != null && count > 0;
    }

    public boolean detallePrescripcionExistsInEmpresa(Long detalle_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM detalle_prescripcion
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND activo     = true
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", detalle_id)
                .addValue("empresa_id", empresa_id), Long.class);
        return count != null && count > 0;
    }

    public boolean dispensacionExistsInTenant(Long dispensacion_id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT COUNT(*)
                FROM dispensacion
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND sede_id    = :sede_id
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", dispensacion_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id), Long.class);
        return count != null && count > 0;
    }

    public boolean loteExistsInEmpresa(Long lote_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM lote
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", lote_id)
                .addValue("empresa_id", empresa_id), Long.class);
        return count != null && count > 0;
    }

    public boolean viaAdministracionExists(Long via_id) {
        String sql = "SELECT COUNT(*) FROM via_administracion WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource().addValue("id", via_id), Long.class);
        return count != null && count > 0;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private AdministracionMedicamentoResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return AdministracionMedicamentoResponseDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .prescriptionDetailId(toLong(row.get("detalle_prescripcion_id")))
                .medicationName((String) row.get("medicamento_nombre"))
                .dispensationId(toLong(row.get("dispensacion_id")))
                .dispensationNumber((String) row.get("dispensacion_numero"))
                .loteId(toLong(row.get("lote_id")))
                .loteNumber((String) row.get("lote_numero"))
                .professionalId(toLong(row.get("profesional_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .scheduledAt(toLocalDateTime(row.get("fecha_programada")))
                .administeredAt(toLocalDateTime(row.get("fecha_administracion")))
                .administeredDose(toBigDecimal(row.get("dosis_administrada")))
                .routeOfAdministrationId(toLong(row.get("via_administracion_id")))
                .routeOfAdministrationName((String) row.get("via_nombre"))
                .status((String) row.get("estado"))
                .omissionReason((String) row.get("motivo_omision"))
                .adverseReaction((String) row.get("reaccion_adversa"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private AdministracionMedicamentoTableDto mapRowToTableDto(Map<String, Object> row) {
        String reaction = (String) row.get("reaccion_adversa");
        boolean hasReaction = reaction != null && !reaction.isBlank();
        return AdministracionMedicamentoTableDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .prescriptionDetailId(toLong(row.get("detalle_prescripcion_id")))
                .medicationName((String) row.get("medicamento_nombre"))
                .professionalName((String) row.get("profesional_nombre"))
                .scheduledAt(toLocalDateTime(row.get("fecha_programada")))
                .administeredAt(toLocalDateTime(row.get("fecha_administracion")))
                .administeredDose(toBigDecimal(row.get("dosis_administrada")))
                .status((String) row.get("estado"))
                .hasAdverseReaction(hasReaction)
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
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

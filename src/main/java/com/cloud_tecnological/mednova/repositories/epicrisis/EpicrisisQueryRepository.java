package com.cloud_tecnological.mednova.repositories.epicrisis;

import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisFilterParams;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisPreloadDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisResponseDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EpicrisisQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EpicrisisQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-091: Lectura por ID ─────────────────────────────────────────

    public Optional<EpicrisisResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT e.id,
                       e.admision_id,
                       adm.numero_admision,
                       e.paciente_id,
                       e.profesional_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       e.fecha_egreso,
                       e.motivo_ingreso,
                       e.diagnostico_ingreso,
                       e.diagnostico_egreso,
                       e.procedimientos_realizados,
                       e.evolucion_resumen,
                       e.complicaciones,
                       e.plan_seguimiento,
                       e.medicamentos_egreso,
                       e.recomendaciones,
                       e.indicaciones_dieta,
                       e.indicaciones_actividad,
                       e.fecha_proximo_control,
                       e.firmada,
                       e.fecha_firma,
                       e.pdf_url,
                       e.activo,
                       e.created_at,
                       e.updated_at,
                       e.usuario_creacion,
                       e.usuario_modificacion
                FROM epicrisis e
                LEFT JOIN admision          adm ON adm.id = e.admision_id
                LEFT JOIN profesional_salud ps  ON ps.id  = e.profesional_id
                LEFT JOIN tercero           te  ON te.id  = ps.tercero_id
                WHERE e.id         = :id
                  AND e.empresa_id = :empresa_id
                  AND e.sede_id    = :sede_id
                  AND e.deleted_at IS NULL
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

    public PageImpl<EpicrisisTableDto> listEpicrisis(
            PageableDto<EpicrisisFilterParams> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        EpicrisisFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT e.id,
                       e.admision_id,
                       adm.numero_admision,
                       e.paciente_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       e.fecha_egreso,
                       e.firmada,
                       e.activo,
                       COUNT(*) OVER() AS total_rows
                FROM epicrisis e
                LEFT JOIN admision          adm ON adm.id = e.admision_id
                LEFT JOIN profesional_salud ps  ON ps.id  = e.profesional_id
                LEFT JOIN tercero           te  ON te.id  = ps.tercero_id
                WHERE e.empresa_id = :empresa_id
                  AND e.sede_id    = :sede_id
                  AND e.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (filter != null) {
            if (filter.getAdmissionId() != null) {
                sql.append(" AND e.admision_id = :adm_id ");
                params.addValue("adm_id", filter.getAdmissionId());
            }
            if (filter.getPatientId() != null) {
                sql.append(" AND e.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getProfessionalId() != null) {
                sql.append(" AND e.profesional_id = :prof_id ");
                params.addValue("prof_id", filter.getProfessionalId());
            }
            if (filter.getSigned() != null) {
                sql.append(" AND e.firmada = :firmada ");
                params.addValue("firmada", filter.getSigned());
            }
            if (filter.getDischargeFrom() != null) {
                sql.append(" AND e.fecha_egreso >= :from ");
                params.addValue("from", filter.getDischargeFrom().atStartOfDay());
            }
            if (filter.getDischargeTo() != null) {
                sql.append(" AND e.fecha_egreso < :to ");
                params.addValue("to", filter.getDischargeTo().plusDays(1).atStartOfDay());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND e.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(adm.numero_admision)    LIKE UPPER(:search)
                        OR UPPER(e.diagnostico_egreso) LIKE UPPER(:search)
                        OR UPPER(e.motivo_ingreso)     LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "e.fecha_egreso";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<EpicrisisTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── HU-FASE2-091 CA2: precarga desde admisión ───────────────────────────

    public Optional<EpicrisisPreloadDto> preloadFromAdmision(Long admision_id, Long empresa_id, Long sede_id) {
        String sqlAdm = """
                SELECT adm.id,
                       adm.numero_admision,
                       adm.paciente_id,
                       (tp.primer_nombre || ' ' || tp.primer_apellido) AS paciente_nombre,
                       adm.fecha_admision,
                       adm.fecha_egreso,
                       adm.motivo_ingreso
                FROM admision adm
                LEFT JOIN paciente pac ON pac.id = adm.paciente_id
                LEFT JOIN tercero  tp  ON tp.id  = pac.tercero_id
                WHERE adm.id         = :id
                  AND adm.empresa_id = :empresa_id
                  AND adm.sede_id    = :sede_id
                  AND adm.deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> admRows = jdbc.query(sqlAdm, new MapSqlParameterSource()
                .addValue("id", admision_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id), new ColumnMapRowMapper());
        if (admRows.isEmpty()) return Optional.empty();
        Map<String, Object> adm = admRows.get(0);

        // Diagnósticos registrados durante la admisión.
        String sqlDiag = """
                SELECT DISTINCT (cd.codigo || ' - ' || cd.descripcion) AS diag
                FROM diagnostico_atencion da
                INNER JOIN atencion             a   ON a.id  = da.atencion_id
                LEFT  JOIN catalogo_diagnostico cd  ON cd.id = da.catalogo_diagnostico_id
                WHERE a.admision_id = :admision_id
                  AND a.empresa_id  = :empresa_id
                  AND da.deleted_at IS NULL
                  AND a.deleted_at  IS NULL
                ORDER BY 1
                """;
        List<String> diagnoses = jdbc.queryForList(sqlDiag, new MapSqlParameterSource()
                .addValue("admision_id", admision_id)
                .addValue("empresa_id", empresa_id), String.class);

        // Procedimientos realizados (servicio_salud) vía ordenes ejecutadas / facturadas.
        String sqlProc = """
                SELECT DISTINCT (ss.codigo || ' - ' || ss.nombre) AS proc
                FROM detalle_orden_clinica doc
                INNER JOIN orden_clinica       oc ON oc.id = doc.orden_clinica_id
                LEFT  JOIN servicio_salud      ss ON ss.id = doc.servicio_salud_id
                INNER JOIN atencion            a  ON a.id  = oc.atencion_id
                WHERE a.admision_id = :admision_id
                  AND oc.empresa_id = :empresa_id
                  AND oc.deleted_at IS NULL
                  AND a.deleted_at  IS NULL
                ORDER BY 1
                """;
        List<String> procedures;
        try {
            procedures = jdbc.queryForList(sqlProc, new MapSqlParameterSource()
                    .addValue("admision_id", admision_id)
                    .addValue("empresa_id", empresa_id), String.class);
        } catch (Exception ex) {
            procedures = List.of();
        }

        EpicrisisPreloadDto dto = EpicrisisPreloadDto.builder()
                .admissionId(toLong(adm.get("id")))
                .admissionNumber((String) adm.get("numero_admision"))
                .patientId(toLong(adm.get("paciente_id")))
                .patientName((String) adm.get("paciente_nombre"))
                .admissionDate(toLocalDateTime(adm.get("fecha_admision")))
                .dischargeDate(toLocalDateTime(adm.get("fecha_egreso")))
                .admissionReason((String) adm.get("motivo_ingreso"))
                .admissionDiagnoses(diagnoses)
                .performedProcedures(procedures)
                .build();
        return Optional.of(dto);
    }

    // ── Validaciones cross-tenant ───────────────────────────────────────────

    /** Devuelve la admisión válida (con fecha_egreso) si existe en empresa+sede. */
    public Optional<Map<String, Object>> findAdmisionEgresada(Long admision_id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT id, paciente_id, fecha_egreso, motivo_ingreso, numero_admision
                FROM admision
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND sede_id    = :sede_id
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("id", admision_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(rows.get(0));
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

    public boolean epicrisisExistsForAdmision(Long admision_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM epicrisis
                WHERE admision_id = :id
                  AND empresa_id  = :empresa_id
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", admision_id)
                .addValue("empresa_id", empresa_id), Long.class);
        return count != null && count > 0;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private EpicrisisResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return EpicrisisResponseDto.builder()
                .id(toLong(row.get("id")))
                .admissionId(toLong(row.get("admision_id")))
                .admissionNumber((String) row.get("numero_admision"))
                .patientId(toLong(row.get("paciente_id")))
                .professionalId(toLong(row.get("profesional_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .dischargeDate(toLocalDateTime(row.get("fecha_egreso")))
                .admissionReason((String) row.get("motivo_ingreso"))
                .admissionDiagnosis((String) row.get("diagnostico_ingreso"))
                .dischargeDiagnosis((String) row.get("diagnostico_egreso"))
                .proceduresPerformed((String) row.get("procedimientos_realizados"))
                .evolutionSummary((String) row.get("evolucion_resumen"))
                .complications((String) row.get("complicaciones"))
                .followUpPlan((String) row.get("plan_seguimiento"))
                .dischargeMedications((String) row.get("medicamentos_egreso"))
                .recommendations((String) row.get("recomendaciones"))
                .dietInstructions((String) row.get("indicaciones_dieta"))
                .activityInstructions((String) row.get("indicaciones_actividad"))
                .nextControlDate(toLocalDate(row.get("fecha_proximo_control")))
                .signed((Boolean) row.get("firmada"))
                .signedAt(toLocalDateTime(row.get("fecha_firma")))
                .pdfUrl((String) row.get("pdf_url"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private EpicrisisTableDto mapRowToTableDto(Map<String, Object> row) {
        return EpicrisisTableDto.builder()
                .id(toLong(row.get("id")))
                .admissionId(toLong(row.get("admision_id")))
                .admissionNumber((String) row.get("numero_admision"))
                .patientId(toLong(row.get("paciente_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .dischargeDate(toLocalDateTime(row.get("fecha_egreso")))
                .signed((Boolean) row.get("firmada"))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
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

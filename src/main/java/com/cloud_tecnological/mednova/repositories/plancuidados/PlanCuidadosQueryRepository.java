package com.cloud_tecnological.mednova.repositories.plancuidados;

import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosFilterParams;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosResponseDto;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosTableDto;
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
public class PlanCuidadosQueryRepository {

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final NamedParameterJdbcTemplate jdbc;

    public PlanCuidadosQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-085: Lectura por ID ────────────────────────────────────────

    public Optional<PlanCuidadosResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT p.id,
                       p.atencion_id,
                       p.paciente_id,
                       p.profesional_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       p.fecha_plan,
                       p.diagnostico_enfermeria,
                       p.objetivos,
                       p.intervenciones,
                       p.evaluacion,
                       p.estado,
                       p.activo,
                       p.created_at,
                       p.updated_at,
                       p.usuario_creacion,
                       p.usuario_modificacion
                FROM plan_cuidados_enfermeria p
                LEFT JOIN profesional_salud ps ON ps.id = p.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE p.id         = :id
                  AND p.empresa_id = :empresa_id
                  AND p.sede_id    = :sede_id
                  AND p.deleted_at IS NULL
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

    public PageImpl<PlanCuidadosTableDto> listPlanes(
            PageableDto<PlanCuidadosFilterParams> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        PlanCuidadosFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT p.id,
                       p.atencion_id,
                       p.paciente_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       p.fecha_plan,
                       p.diagnostico_enfermeria,
                       p.estado,
                       p.activo,
                       COUNT(*) OVER()        AS total_rows
                FROM plan_cuidados_enfermeria p
                LEFT JOIN profesional_salud ps ON ps.id = p.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE p.empresa_id = :empresa_id
                  AND p.sede_id    = :sede_id
                  AND p.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (filter != null) {
            if (filter.getEncounterId() != null) {
                sql.append(" AND p.atencion_id = :atencion_id ");
                params.addValue("atencion_id", filter.getEncounterId());
            }
            if (filter.getPatientId() != null) {
                sql.append(" AND p.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getProfessionalId() != null) {
                sql.append(" AND p.profesional_id = :profesional_id ");
                params.addValue("profesional_id", filter.getProfessionalId());
            }
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                sql.append(" AND p.estado = :estado ");
                params.addValue("estado", filter.getStatus().trim());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActiveStatus())) {
                sql.append(" AND p.estado = :estado_activo ");
                params.addValue("estado_activo", ESTADO_ACTIVO);
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND p.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(p.diagnostico_enfermeria) LIKE UPPER(:search)
                        OR UPPER(p.objetivos)           LIKE UPPER(:search)
                        OR UPPER(p.intervenciones)      LIKE UPPER(:search)
                        OR UPPER(p.evaluacion)          LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "p.fecha_plan";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<PlanCuidadosTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
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

    // ── Mappers ──────────────────────────────────────────────────────────────

    private PlanCuidadosResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return PlanCuidadosResponseDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .professionalId(toLong(row.get("profesional_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .planDate(toLocalDate(row.get("fecha_plan")))
                .nursingDiagnosis((String) row.get("diagnostico_enfermeria"))
                .objectives((String) row.get("objetivos"))
                .interventions((String) row.get("intervenciones"))
                .evaluation((String) row.get("evaluacion"))
                .status((String) row.get("estado"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private PlanCuidadosTableDto mapRowToTableDto(Map<String, Object> row) {
        return PlanCuidadosTableDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .planDate(toLocalDate(row.get("fecha_plan")))
                .nursingDiagnosis((String) row.get("diagnostico_enfermeria"))
                .status((String) row.get("estado"))
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

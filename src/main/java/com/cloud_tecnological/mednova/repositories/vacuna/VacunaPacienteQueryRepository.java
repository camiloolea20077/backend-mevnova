package com.cloud_tecnological.mednova.repositories.vacuna;

import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteFilterParams;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteTableDto;
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
public class VacunaPacienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public VacunaPacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-083: Lectura por ID ────────────────────────────────────────

    public Optional<VacunaPacienteResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT v.id,
                       v.paciente_id,
                       v.nombre_vacuna,
                       v.codigo_vacuna,
                       v.dosis,
                       v.total_dosis_esquema,
                       v.fecha_aplicacion,
                       v.fecha_proxima_dosis,
                       v.laboratorio,
                       v.numero_lote,
                       v.via_administracion_id,
                       va.nombre                AS via_nombre,
                       v.profesional_aplica_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       v.institucion_aplica,
                       v.observaciones,
                       v.activo,
                       v.created_at,
                       v.updated_at,
                       v.usuario_creacion,
                       v.usuario_modificacion
                FROM vacuna_paciente v
                LEFT JOIN via_administracion va ON va.id = v.via_administracion_id
                LEFT JOIN profesional_salud  ps ON ps.id = v.profesional_aplica_id
                LEFT JOIN tercero            te ON te.id = ps.tercero_id
                WHERE v.id         = :id
                  AND v.empresa_id = :empresa_id
                  AND v.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public PageImpl<VacunaPacienteTableDto> listVacunas(
            PageableDto<VacunaPacienteFilterParams> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        VacunaPacienteFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT v.id,
                       v.paciente_id,
                       v.nombre_vacuna,
                       v.codigo_vacuna,
                       v.dosis,
                       v.total_dosis_esquema,
                       v.fecha_aplicacion,
                       v.fecha_proxima_dosis,
                       v.laboratorio,
                       va.nombre            AS via_nombre,
                       v.institucion_aplica,
                       v.activo,
                       COUNT(*) OVER()      AS total_rows
                FROM vacuna_paciente v
                LEFT JOIN via_administracion va ON va.id = v.via_administracion_id
                WHERE v.empresa_id = :empresa_id
                  AND v.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (filter != null) {
            if (filter.getPatientId() != null) {
                sql.append(" AND v.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getVaccineCode() != null && !filter.getVaccineCode().isBlank()) {
                sql.append(" AND UPPER(v.codigo_vacuna) = UPPER(:codigo_vacuna) ");
                params.addValue("codigo_vacuna", filter.getVaccineCode().trim());
            }
            if (filter.getNextDoseFrom() != null) {
                sql.append(" AND v.fecha_proxima_dosis >= :next_from ");
                params.addValue("next_from", java.sql.Date.valueOf(filter.getNextDoseFrom()));
            }
            if (filter.getNextDoseTo() != null) {
                sql.append(" AND v.fecha_proxima_dosis <= :next_to ");
                params.addValue("next_to", java.sql.Date.valueOf(filter.getNextDoseTo()));
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND v.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(v.nombre_vacuna)        LIKE UPPER(:search)
                        OR UPPER(v.codigo_vacuna)     LIKE UPPER(:search)
                        OR UPPER(v.laboratorio)       LIKE UPPER(:search)
                        OR UPPER(v.numero_lote)       LIKE UPPER(:search)
                        OR UPPER(v.institucion_aplica) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "v.fecha_aplicacion";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<VacunaPacienteTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Validaciones cross-tenant ───────────────────────────────────────────

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

    public boolean viaAdministracionExists(Long via_id) {
        String sql = "SELECT COUNT(*) FROM via_administracion WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", via_id), Long.class);
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

    private VacunaPacienteResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return VacunaPacienteResponseDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .vaccineName((String) row.get("nombre_vacuna"))
                .vaccineCode((String) row.get("codigo_vacuna"))
                .doseNumber(toInteger(row.get("dosis")))
                .totalSchemeDoses(toInteger(row.get("total_dosis_esquema")))
                .applicationDate(toLocalDate(row.get("fecha_aplicacion")))
                .nextDoseDate(toLocalDate(row.get("fecha_proxima_dosis")))
                .laboratory((String) row.get("laboratorio"))
                .batchNumber((String) row.get("numero_lote"))
                .administrationRouteId(toLong(row.get("via_administracion_id")))
                .administrationRouteName((String) row.get("via_nombre"))
                .applyingProfessionalId(toLong(row.get("profesional_aplica_id")))
                .applyingProfessionalName((String) row.get("profesional_nombre"))
                .applyingInstitution((String) row.get("institucion_aplica"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private VacunaPacienteTableDto mapRowToTableDto(Map<String, Object> row) {
        return VacunaPacienteTableDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .vaccineName((String) row.get("nombre_vacuna"))
                .vaccineCode((String) row.get("codigo_vacuna"))
                .doseNumber(toInteger(row.get("dosis")))
                .totalSchemeDoses(toInteger(row.get("total_dosis_esquema")))
                .applicationDate(toLocalDate(row.get("fecha_aplicacion")))
                .nextDoseDate(toLocalDate(row.get("fecha_proxima_dosis")))
                .laboratory((String) row.get("laboratorio"))
                .administrationRouteName((String) row.get("via_nombre"))
                .applyingInstitution((String) row.get("institucion_aplica"))
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

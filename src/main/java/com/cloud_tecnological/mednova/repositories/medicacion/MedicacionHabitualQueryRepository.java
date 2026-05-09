package com.cloud_tecnological.mednova.repositories.medicacion;

import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualFilterParams;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualResponseDto;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualTableDto;
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
public class MedicacionHabitualQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MedicacionHabitualQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-084: Lectura por ID ────────────────────────────────────────

    public Optional<MedicacionHabitualResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT m.id,
                       m.paciente_id,
                       m.servicio_salud_id,
                       ss.nombre                AS servicio_nombre,
                       m.nombre_medicamento,
                       m.dosis,
                       m.via_administracion_id,
                       va.nombre                AS via_nombre,
                       m.frecuencia_dosis_id,
                       fd.nombre                AS frecuencia_nombre,
                       m.fecha_inicio,
                       m.fecha_fin,
                       m.indicacion,
                       m.profesional_prescriptor,
                       m.es_activo,
                       m.observaciones,
                       m.activo,
                       m.created_at,
                       m.updated_at,
                       m.usuario_creacion,
                       m.usuario_modificacion
                FROM medicacion_habitual m
                LEFT JOIN servicio_salud      ss ON ss.id = m.servicio_salud_id
                LEFT JOIN via_administracion  va ON va.id = m.via_administracion_id
                LEFT JOIN frecuencia_dosis    fd ON fd.id = m.frecuencia_dosis_id
                WHERE m.id         = :id
                  AND m.empresa_id = :empresa_id
                  AND m.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public PageImpl<MedicacionHabitualTableDto> listMedicaciones(
            PageableDto<MedicacionHabitualFilterParams> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        MedicacionHabitualFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT m.id,
                       m.paciente_id,
                       m.nombre_medicamento,
                       m.dosis,
                       va.nombre                AS via_nombre,
                       fd.nombre                AS frecuencia_nombre,
                       m.fecha_inicio,
                       m.fecha_fin,
                       m.es_activo,
                       m.profesional_prescriptor,
                       m.activo,
                       COUNT(*) OVER()          AS total_rows
                FROM medicacion_habitual m
                LEFT JOIN via_administracion va ON va.id = m.via_administracion_id
                LEFT JOIN frecuencia_dosis   fd ON fd.id = m.frecuencia_dosis_id
                WHERE m.empresa_id = :empresa_id
                  AND m.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (filter != null) {
            if (filter.getPatientId() != null) {
                sql.append(" AND m.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (Boolean.TRUE.equals(filter.getOnlyCurrentlyTaking())) {
                sql.append(" AND m.es_activo = true ");
            }
            if (filter.getHealthServiceId() != null) {
                sql.append(" AND m.servicio_salud_id = :servicio_id ");
                params.addValue("servicio_id", filter.getHealthServiceId());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND m.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(m.nombre_medicamento)     LIKE UPPER(:search)
                        OR UPPER(m.indicacion)          LIKE UPPER(:search)
                        OR UPPER(m.profesional_prescriptor) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "m.created_at";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<MedicacionHabitualTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
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

    public boolean servicioSaludExists(Long servicio_id) {
        String sql = "SELECT COUNT(*) FROM servicio_salud WHERE id = :id AND activo = true AND deleted_at IS NULL";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", servicio_id), Long.class);
        return count != null && count > 0;
    }

    public boolean viaAdministracionExists(Long via_id) {
        String sql = "SELECT COUNT(*) FROM via_administracion WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", via_id), Long.class);
        return count != null && count > 0;
    }

    public boolean frecuenciaDosisExists(Long frecuencia_id) {
        String sql = "SELECT COUNT(*) FROM frecuencia_dosis WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", frecuencia_id), Long.class);
        return count != null && count > 0;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private MedicacionHabitualResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return MedicacionHabitualResponseDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .healthServiceId(toLong(row.get("servicio_salud_id")))
                .healthServiceName((String) row.get("servicio_nombre"))
                .medicationName((String) row.get("nombre_medicamento"))
                .dose((String) row.get("dosis"))
                .administrationRouteId(toLong(row.get("via_administracion_id")))
                .administrationRouteName((String) row.get("via_nombre"))
                .doseFrequencyId(toLong(row.get("frecuencia_dosis_id")))
                .doseFrequencyName((String) row.get("frecuencia_nombre"))
                .startDate(toLocalDate(row.get("fecha_inicio")))
                .endDate(toLocalDate(row.get("fecha_fin")))
                .indication((String) row.get("indicacion"))
                .prescribingProfessional((String) row.get("profesional_prescriptor"))
                .isCurrentlyTaking((Boolean) row.get("es_activo"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private MedicacionHabitualTableDto mapRowToTableDto(Map<String, Object> row) {
        return MedicacionHabitualTableDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .medicationName((String) row.get("nombre_medicamento"))
                .dose((String) row.get("dosis"))
                .administrationRouteName((String) row.get("via_nombre"))
                .doseFrequencyName((String) row.get("frecuencia_nombre"))
                .startDate(toLocalDate(row.get("fecha_inicio")))
                .endDate(toLocalDate(row.get("fecha_fin")))
                .isCurrentlyTaking((Boolean) row.get("es_activo"))
                .prescribingProfessional((String) row.get("profesional_prescriptor"))
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

package com.cloud_tecnological.mednova.repositories.habito;

import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteFilterParams;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteTableDto;
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
public class HabitoPacienteQueryRepository {

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final NamedParameterJdbcTemplate jdbc;

    public HabitoPacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-081: Lectura de hábito por ID ──────────────────────────────

    public Optional<HabitoPacienteResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT h.id,
                       h.paciente_id,
                       h.tipo_habito,
                       h.descripcion,
                       h.frecuencia,
                       h.cantidad,
                       h.tiempo_consumo,
                       h.fecha_inicio,
                       h.fecha_fin,
                       h.estado,
                       h.observaciones,
                       h.activo,
                       h.created_at,
                       h.updated_at,
                       h.usuario_creacion,
                       h.usuario_modificacion
                FROM habito_paciente h
                WHERE h.id         = :id
                  AND h.empresa_id = :empresa_id
                  AND h.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public PageImpl<HabitoPacienteTableDto> listHabitos(
            PageableDto<HabitoPacienteFilterParams> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        HabitoPacienteFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT h.id,
                       h.paciente_id,
                       h.tipo_habito,
                       h.descripcion,
                       h.frecuencia,
                       h.cantidad,
                       h.estado,
                       h.fecha_inicio,
                       h.fecha_fin,
                       h.activo,
                       COUNT(*) OVER()      AS total_rows
                FROM habito_paciente h
                WHERE h.empresa_id = :empresa_id
                  AND h.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (filter != null) {
            if (filter.getPatientId() != null) {
                sql.append(" AND h.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getHabitType() != null && !filter.getHabitType().isBlank()) {
                sql.append(" AND h.tipo_habito = :tipo_habito ");
                params.addValue("tipo_habito", filter.getHabitType().trim());
            }
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                sql.append(" AND h.estado = :estado ");
                params.addValue("estado", filter.getStatus().trim());
            }
            if (Boolean.TRUE.equals(filter.getOnlyCurrentlyActive())) {
                sql.append(" AND h.estado = :estado_activo ");
                params.addValue("estado_activo", ESTADO_ACTIVO);
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND h.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(h.descripcion)   LIKE UPPER(:search)
                        OR UPPER(h.tipo_habito) LIKE UPPER(:search)
                        OR UPPER(h.frecuencia) LIKE UPPER(:search)
                        OR UPPER(h.cantidad)   LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "h.created_at";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<HabitoPacienteTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Validaciones cross-tenant y reglas ──────────────────────────────────

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

    /** Existe otro registro en estado ACTIVO para el mismo paciente y tipo (excluyendo el id dado). */
    public boolean activeHabitExistsForType(Long paciente_id, String tipo_habito,
                                            Long empresa_id, Long excludeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM habito_paciente
                WHERE empresa_id  = :empresa_id
                  AND paciente_id = :paciente_id
                  AND tipo_habito = :tipo_habito
                  AND estado      = :estado
                  AND deleted_at IS NULL
                """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("paciente_id", paciente_id)
                .addValue("tipo_habito", tipo_habito)
                .addValue("estado", ESTADO_ACTIVO);

        if (excludeId != null) {
            sql.append(" AND id <> :exclude_id ");
            params.addValue("exclude_id", excludeId);
        }

        Long count = jdbc.queryForObject(sql.toString(), params, Long.class);
        return count != null && count > 0;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private HabitoPacienteResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return HabitoPacienteResponseDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .habitType((String) row.get("tipo_habito"))
                .description((String) row.get("descripcion"))
                .frequency((String) row.get("frecuencia"))
                .quantity((String) row.get("cantidad"))
                .consumptionTime((String) row.get("tiempo_consumo"))
                .startDate(toLocalDate(row.get("fecha_inicio")))
                .endDate(toLocalDate(row.get("fecha_fin")))
                .status((String) row.get("estado"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private HabitoPacienteTableDto mapRowToTableDto(Map<String, Object> row) {
        return HabitoPacienteTableDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .habitType((String) row.get("tipo_habito"))
                .description((String) row.get("descripcion"))
                .frequency((String) row.get("frecuencia"))
                .quantity((String) row.get("cantidad"))
                .status((String) row.get("estado"))
                .startDate(toLocalDate(row.get("fecha_inicio")))
                .endDate(toLocalDate(row.get("fecha_fin")))
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

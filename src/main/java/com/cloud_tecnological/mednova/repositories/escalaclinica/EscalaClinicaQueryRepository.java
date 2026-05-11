package com.cloud_tecnological.mednova.repositories.escalaclinica;

import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaFilterParams;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaResponseDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EscalaClinicaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public EscalaClinicaQueryRepository(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    // ── HU-FASE2-089: Lectura por ID ─────────────────────────────────────────

    public Optional<EscalaClinicaResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT e.id,
                       e.atencion_id,
                       e.paciente_id,
                       e.profesional_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       e.tipo_escala,
                       e.fecha_aplicacion,
                       e.puntaje_total,
                       e.interpretacion,
                       e.riesgo,
                       e.detalle_escala,
                       e.observaciones,
                       e.activo,
                       e.created_at,
                       e.updated_at,
                       e.usuario_creacion,
                       e.usuario_modificacion
                FROM escala_clinica e
                LEFT JOIN profesional_salud ps ON ps.id = e.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
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

    public PageImpl<EscalaClinicaTableDto> listEscalas(
            PageableDto<EscalaClinicaFilterParams> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        EscalaClinicaFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT e.id,
                       e.atencion_id,
                       e.paciente_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       e.tipo_escala,
                       e.fecha_aplicacion,
                       e.puntaje_total,
                       e.interpretacion,
                       e.riesgo,
                       e.activo,
                       COUNT(*) OVER() AS total_rows
                FROM escala_clinica e
                LEFT JOIN profesional_salud ps ON ps.id = e.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE e.empresa_id = :empresa_id
                  AND e.sede_id    = :sede_id
                  AND e.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (filter != null) {
            if (filter.getEncounterId() != null) {
                sql.append(" AND e.atencion_id = :atencion_id ");
                params.addValue("atencion_id", filter.getEncounterId());
            }
            if (filter.getPatientId() != null) {
                sql.append(" AND e.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getProfessionalId() != null) {
                sql.append(" AND e.profesional_id = :profesional_id ");
                params.addValue("profesional_id", filter.getProfessionalId());
            }
            if (filter.getScaleType() != null && !filter.getScaleType().isBlank()) {
                sql.append(" AND e.tipo_escala = :tipo_escala ");
                params.addValue("tipo_escala", filter.getScaleType().trim());
            }
            if (filter.getRisk() != null && !filter.getRisk().isBlank()) {
                sql.append(" AND e.riesgo = :riesgo ");
                params.addValue("riesgo", filter.getRisk().trim());
            }
            if (filter.getDateFrom() != null) {
                sql.append(" AND e.fecha_aplicacion >= :date_from ");
                params.addValue("date_from", filter.getDateFrom().atStartOfDay());
            }
            if (filter.getDateTo() != null) {
                sql.append(" AND e.fecha_aplicacion < :date_to ");
                params.addValue("date_to", filter.getDateTo().plusDays(1).atStartOfDay());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND e.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(e.interpretacion) LIKE UPPER(:search)
                        OR UPPER(e.observaciones) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "e.fecha_aplicacion";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<EscalaClinicaTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
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

    private EscalaClinicaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return EscalaClinicaResponseDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .professionalId(toLong(row.get("profesional_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .scaleType((String) row.get("tipo_escala"))
                .appliedAt(toLocalDateTime(row.get("fecha_aplicacion")))
                .totalScore(toInteger(row.get("puntaje_total")))
                .interpretation((String) row.get("interpretacion"))
                .risk((String) row.get("riesgo"))
                .scaleDetail(parseJson(row.get("detalle_escala")))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private EscalaClinicaTableDto mapRowToTableDto(Map<String, Object> row) {
        return EscalaClinicaTableDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .scaleType((String) row.get("tipo_escala"))
                .appliedAt(toLocalDateTime(row.get("fecha_aplicacion")))
                .totalScore(toInteger(row.get("puntaje_total")))
                .interpretation((String) row.get("interpretacion"))
                .risk((String) row.get("riesgo"))
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

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }

    private JsonNode parseJson(Object value) {
        if (value == null) return null;
        try {
            String raw;
            if (value instanceof PGobject pg) {
                raw = pg.getValue();
            } else {
                raw = value.toString();
            }
            if (raw == null || raw.isBlank()) return null;
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            return null;
        }
    }
}

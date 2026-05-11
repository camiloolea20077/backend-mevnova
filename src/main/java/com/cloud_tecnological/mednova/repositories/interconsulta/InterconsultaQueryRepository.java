package com.cloud_tecnological.mednova.repositories.interconsulta;

import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaFilterParams;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaResponseDto;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
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
public class InterconsultaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public InterconsultaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-090: lectura por ID ─────────────────────────────────────────

    /** findActiveById con isolation E+S por defecto. Si crossSede=true, valida solo empresa. */
    public Optional<InterconsultaResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id, boolean crossSede) {
        StringBuilder sql = new StringBuilder("""
                SELECT i.id,
                       i.numero_interconsulta,
                       i.atencion_origen_id,
                       i.atencion_respuesta_id,
                       i.profesional_solicita_id,
                       (ts.primer_nombre || ' ' || ts.primer_apellido) AS solicita_nombre,
                       i.profesional_responde_id,
                       (tr.primer_nombre || ' ' || tr.primer_apellido) AS responde_nombre,
                       i.especialidad_destino_id,
                       esp.nombre                AS especialidad_nombre,
                       i.motivo,
                       i.impresion_diagnostica,
                       i.pregunta_clinica,
                       i.estado,
                       i.prioridad,
                       i.fecha_solicitud,
                       i.fecha_respuesta,
                       i.respuesta,
                       i.recomendaciones,
                       i.activo,
                       i.created_at,
                       i.updated_at,
                       i.usuario_creacion,
                       i.usuario_modificacion
                FROM interconsulta i
                LEFT JOIN profesional_salud ps_s ON ps_s.id = i.profesional_solicita_id
                LEFT JOIN tercero           ts   ON ts.id   = ps_s.tercero_id
                LEFT JOIN profesional_salud ps_r ON ps_r.id = i.profesional_responde_id
                LEFT JOIN tercero           tr   ON tr.id   = ps_r.tercero_id
                LEFT JOIN especialidad      esp  ON esp.id  = i.especialidad_destino_id
                WHERE i.id         = :id
                  AND i.empresa_id = :empresa_id
                  AND i.deleted_at IS NULL
                """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        if (!crossSede) {
            sql.append(" AND i.sede_id = :sede_id ");
            params.addValue("sede_id", sede_id);
        }
        sql.append(" LIMIT 1");

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public PageImpl<InterconsultaTableDto> listInterconsultas(
            PageableDto<InterconsultaFilterParams> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        InterconsultaFilterParams filter = pageable.getParams();

        boolean crossSede = filter != null && Boolean.TRUE.equals(filter.getCrossSede());

        StringBuilder sql = new StringBuilder("""
                SELECT i.id,
                       i.numero_interconsulta,
                       i.atencion_origen_id,
                       (ts.primer_nombre || ' ' || ts.primer_apellido) AS solicita_nombre,
                       esp.nombre                AS especialidad_nombre,
                       i.estado,
                       i.prioridad,
                       i.fecha_solicitud,
                       i.fecha_respuesta,
                       i.activo,
                       COUNT(*) OVER()           AS total_rows
                FROM interconsulta i
                LEFT JOIN profesional_salud ps_s ON ps_s.id = i.profesional_solicita_id
                LEFT JOIN tercero           ts   ON ts.id   = ps_s.tercero_id
                LEFT JOIN especialidad      esp  ON esp.id  = i.especialidad_destino_id
                WHERE i.empresa_id = :empresa_id
                  AND i.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("empresa_id", empresa_id);

        if (!crossSede) {
            sql.append(" AND i.sede_id = :sede_id ");
            params.addValue("sede_id", sede_id);
        }

        if (filter != null) {
            if (filter.getOriginEncounterId() != null) {
                sql.append(" AND i.atencion_origen_id = :atencion_id ");
                params.addValue("atencion_id", filter.getOriginEncounterId());
            }
            if (filter.getRequestingProfessionalId() != null) {
                sql.append(" AND i.profesional_solicita_id = :solicita_id ");
                params.addValue("solicita_id", filter.getRequestingProfessionalId());
            }
            if (filter.getRespondingProfessionalId() != null) {
                sql.append(" AND i.profesional_responde_id = :responde_id ");
                params.addValue("responde_id", filter.getRespondingProfessionalId());
            }
            if (filter.getDestinationSpecialtyId() != null) {
                sql.append(" AND i.especialidad_destino_id = :esp_id ");
                params.addValue("esp_id", filter.getDestinationSpecialtyId());
            }
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                sql.append(" AND i.estado = :estado ");
                params.addValue("estado", filter.getStatus().trim());
            }
            if (filter.getPriority() != null && !filter.getPriority().isBlank()) {
                sql.append(" AND i.prioridad = :prioridad ");
                params.addValue("prioridad", filter.getPriority().trim());
            }
            if (Boolean.TRUE.equals(filter.getOnlyPending())) {
                sql.append(" AND i.estado IN ('PENDIENTE', 'EN_PROCESO') ");
            }
            if (filter.getRequestedFrom() != null) {
                sql.append(" AND i.fecha_solicitud >= :req_from ");
                params.addValue("req_from", filter.getRequestedFrom().atStartOfDay());
            }
            if (filter.getRequestedTo() != null) {
                sql.append(" AND i.fecha_solicitud < :req_to ");
                params.addValue("req_to", filter.getRequestedTo().plusDays(1).atStartOfDay());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND i.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(i.numero_interconsulta) LIKE UPPER(:search)
                        OR UPPER(i.motivo)            LIKE UPPER(:search)
                        OR UPPER(i.pregunta_clinica)  LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        // CA2: ordenar por prioridad (VITAL primero) y luego por fecha asc.
        String orderBy = pageable.getOrder_by();
        if (orderBy == null || orderBy.isBlank()) {
            sql.append(" ORDER BY CASE i.prioridad WHEN 'VITAL' THEN 0 WHEN 'URGENTE' THEN 1 WHEN 'NORMAL' THEN 2 ELSE 3 END, i.fecha_solicitud ASC ");
        } else {
            String order = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
            sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        }
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<InterconsultaTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    /** Genera número consecutivo IC-NNNNNN por empresa. */
    public String generateNextNumeroInterconsulta(Long empresa_id) {
        String sql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(numero_interconsulta FROM 'IC-(\\d+)$') AS integer)), 0) + 1
                FROM interconsulta
                WHERE empresa_id = :empresa_id
                  AND numero_interconsulta ~ '^IC-\\d+$'
                """;
        Integer next = jdbc.queryForObject(sql,
                new MapSqlParameterSource().addValue("empresa_id", empresa_id), Integer.class);
        int seq = next == null ? 1 : next;
        return String.format("IC-%06d", seq);
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

    public boolean atencionExistsInEmpresa(Long atencion_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM atencion
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", atencion_id)
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

    public boolean especialidadExists(Long especialidad_id) {
        String sql = "SELECT COUNT(*) FROM especialidad WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource().addValue("id", especialidad_id), Long.class);
        return count != null && count > 0;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private InterconsultaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return InterconsultaResponseDto.builder()
                .id(toLong(row.get("id")))
                .number((String) row.get("numero_interconsulta"))
                .originEncounterId(toLong(row.get("atencion_origen_id")))
                .responseEncounterId(toLong(row.get("atencion_respuesta_id")))
                .requestingProfessionalId(toLong(row.get("profesional_solicita_id")))
                .requestingProfessionalName((String) row.get("solicita_nombre"))
                .respondingProfessionalId(toLong(row.get("profesional_responde_id")))
                .respondingProfessionalName((String) row.get("responde_nombre"))
                .destinationSpecialtyId(toLong(row.get("especialidad_destino_id")))
                .destinationSpecialtyName((String) row.get("especialidad_nombre"))
                .reason((String) row.get("motivo"))
                .diagnosticImpression((String) row.get("impresion_diagnostica"))
                .clinicalQuestion((String) row.get("pregunta_clinica"))
                .status((String) row.get("estado"))
                .priority((String) row.get("prioridad"))
                .requestedAt(toLocalDateTime(row.get("fecha_solicitud")))
                .respondedAt(toLocalDateTime(row.get("fecha_respuesta")))
                .response((String) row.get("respuesta"))
                .recommendations((String) row.get("recomendaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private InterconsultaTableDto mapRowToTableDto(Map<String, Object> row) {
        return InterconsultaTableDto.builder()
                .id(toLong(row.get("id")))
                .number((String) row.get("numero_interconsulta"))
                .originEncounterId(toLong(row.get("atencion_origen_id")))
                .requestingProfessionalName((String) row.get("solicita_nombre"))
                .destinationSpecialtyName((String) row.get("especialidad_nombre"))
                .status((String) row.get("estado"))
                .priority((String) row.get("prioridad"))
                .requestedAt(toLocalDateTime(row.get("fecha_solicitud")))
                .respondedAt(toLocalDateTime(row.get("fecha_respuesta")))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

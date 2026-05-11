package com.cloud_tecnological.mednova.repositories.notaenfermeria;

import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaFilterParams;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaResponseDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaTableDto;
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
public class NotaEnfermeriaQueryRepository {

    private static final int PREVIEW_LENGTH = 160;

    private final NamedParameterJdbcTemplate jdbc;

    public NotaEnfermeriaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-086: Lectura por ID ─────────────────────────────────────────

    public Optional<NotaEnfermeriaResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT n.id,
                       n.atencion_id,
                       n.paciente_id,
                       n.profesional_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       n.tipo_nota,
                       n.turno,
                       n.fecha_nota,
                       n.contenido,
                       n.tension_sistolica,
                       n.tension_diastolica,
                       n.frecuencia_cardiaca,
                       n.frecuencia_respiratoria,
                       n.temperatura,
                       n.saturacion_oxigeno,
                       n.glucometria,
                       n.dolor_eva,
                       n.firmada,
                       n.fecha_firma,
                       n.activo,
                       n.created_at,
                       n.updated_at,
                       n.usuario_creacion,
                       n.usuario_modificacion
                FROM nota_enfermeria n
                LEFT JOIN profesional_salud ps ON ps.id = n.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE n.id         = :id
                  AND n.empresa_id = :empresa_id
                  AND n.sede_id    = :sede_id
                  AND n.deleted_at IS NULL
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

    public PageImpl<NotaEnfermeriaTableDto> listNotas(
            PageableDto<NotaEnfermeriaFilterParams> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        NotaEnfermeriaFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT n.id,
                       n.atencion_id,
                       n.paciente_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       n.tipo_nota,
                       n.turno,
                       n.fecha_nota,
                       n.contenido,
                       n.firmada,
                       n.activo,
                       COUNT(*) OVER() AS total_rows
                FROM nota_enfermeria n
                LEFT JOIN profesional_salud ps ON ps.id = n.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE n.empresa_id = :empresa_id
                  AND n.sede_id    = :sede_id
                  AND n.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (filter != null) {
            if (filter.getEncounterId() != null) {
                sql.append(" AND n.atencion_id = :atencion_id ");
                params.addValue("atencion_id", filter.getEncounterId());
            }
            if (filter.getPatientId() != null) {
                sql.append(" AND n.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getProfessionalId() != null) {
                sql.append(" AND n.profesional_id = :profesional_id ");
                params.addValue("profesional_id", filter.getProfessionalId());
            }
            if (filter.getNoteType() != null && !filter.getNoteType().isBlank()) {
                sql.append(" AND n.tipo_nota = :tipo_nota ");
                params.addValue("tipo_nota", filter.getNoteType().trim());
            }
            if (filter.getShift() != null && !filter.getShift().isBlank()) {
                sql.append(" AND n.turno = :turno ");
                params.addValue("turno", filter.getShift().trim());
            }
            if (filter.getSigned() != null) {
                sql.append(" AND n.firmada = :firmada ");
                params.addValue("firmada", filter.getSigned());
            }
            if (filter.getDateFrom() != null) {
                sql.append(" AND n.fecha_nota >= :date_from ");
                params.addValue("date_from", filter.getDateFrom().atStartOfDay());
            }
            if (filter.getDateTo() != null) {
                sql.append(" AND n.fecha_nota < :date_to ");
                params.addValue("date_to", filter.getDateTo().plusDays(1).atStartOfDay());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND n.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append(" AND UPPER(n.contenido) LIKE UPPER(:search) ");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "n.fecha_nota";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<NotaEnfermeriaTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
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

    private NotaEnfermeriaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return NotaEnfermeriaResponseDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .professionalId(toLong(row.get("profesional_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .noteType((String) row.get("tipo_nota"))
                .shift((String) row.get("turno"))
                .noteDate(toLocalDateTime(row.get("fecha_nota")))
                .content((String) row.get("contenido"))
                .systolicBp(toInteger(row.get("tension_sistolica")))
                .diastolicBp(toInteger(row.get("tension_diastolica")))
                .heartRate(toInteger(row.get("frecuencia_cardiaca")))
                .respiratoryRate(toInteger(row.get("frecuencia_respiratoria")))
                .temperature(toBigDecimal(row.get("temperatura")))
                .oxygenSaturation(toInteger(row.get("saturacion_oxigeno")))
                .glucometry(toBigDecimal(row.get("glucometria")))
                .painEva(toInteger(row.get("dolor_eva")))
                .signed((Boolean) row.get("firmada"))
                .signedAt(toLocalDateTime(row.get("fecha_firma")))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private NotaEnfermeriaTableDto mapRowToTableDto(Map<String, Object> row) {
        String content = (String) row.get("contenido");
        String preview = content == null ? null
                : (content.length() > PREVIEW_LENGTH ? content.substring(0, PREVIEW_LENGTH) + "…" : content);

        return NotaEnfermeriaTableDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .noteType((String) row.get("tipo_nota"))
                .shift((String) row.get("turno"))
                .noteDate(toLocalDateTime(row.get("fecha_nota")))
                .contentPreview(preview)
                .signed((Boolean) row.get("firmada"))
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

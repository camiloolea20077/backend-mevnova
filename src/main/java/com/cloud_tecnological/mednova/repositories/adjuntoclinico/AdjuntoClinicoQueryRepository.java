package com.cloud_tecnological.mednova.repositories.adjuntoclinico;

import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoFilterParams;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoResponseDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoTableDto;
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
public class AdjuntoClinicoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AdjuntoClinicoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-092: Lectura por ID (cross-sede dentro de la empresa) ───────

    public Optional<AdjuntoClinicoResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT a.id,
                       a.paciente_id,
                       a.atencion_id,
                       a.tipo_documento,
                       a.nombre_archivo,
                       a.descripcion,
                       a.url_archivo,
                       a.mime_type,
                       a.tamano_bytes,
                       a.profesional_carga_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       a.fecha_documento,
                       a.es_confidencial,
                       a.activo,
                       a.created_at,
                       a.updated_at,
                       a.usuario_creacion,
                       a.usuario_modificacion
                FROM adjunto_clinico a
                LEFT JOIN profesional_salud ps ON ps.id = a.profesional_carga_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE a.id         = :id
                  AND a.empresa_id = :empresa_id
                  AND a.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    /** Listado paginado. CA3: oculta confidenciales si canSeeConfidential=false. */
    public PageImpl<AdjuntoClinicoTableDto> listAdjuntos(
            PageableDto<AdjuntoClinicoFilterParams> pageable, Long empresa_id, boolean canSeeConfidential) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        AdjuntoClinicoFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT a.id,
                       a.paciente_id,
                       a.atencion_id,
                       a.tipo_documento,
                       a.nombre_archivo,
                       a.mime_type,
                       a.tamano_bytes,
                       a.fecha_documento,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       a.es_confidencial,
                       a.activo,
                       a.created_at,
                       COUNT(*) OVER() AS total_rows
                FROM adjunto_clinico a
                LEFT JOIN profesional_salud ps ON ps.id = a.profesional_carga_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE a.empresa_id = :empresa_id
                  AND a.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("empresa_id", empresa_id);

        if (!canSeeConfidential) {
            sql.append(" AND a.es_confidencial = false ");
        }

        if (filter != null) {
            if (filter.getPatientId() != null) {
                sql.append(" AND a.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getEncounterId() != null) {
                sql.append(" AND a.atencion_id = :atencion_id ");
                params.addValue("atencion_id", filter.getEncounterId());
            }
            if (filter.getDocumentType() != null && !filter.getDocumentType().isBlank()) {
                sql.append(" AND a.tipo_documento = :tipo ");
                params.addValue("tipo", filter.getDocumentType().trim());
            }
            if (filter.getUploadingProfessionalId() != null) {
                sql.append(" AND a.profesional_carga_id = :prof_id ");
                params.addValue("prof_id", filter.getUploadingProfessionalId());
            }
            if (canSeeConfidential && filter.getConfidential() != null) {
                sql.append(" AND a.es_confidencial = :confidencial ");
                params.addValue("confidencial", filter.getConfidential());
            }
            if (filter.getDocumentDateFrom() != null) {
                sql.append(" AND a.fecha_documento >= :from ");
                params.addValue("from", filter.getDocumentDateFrom());
            }
            if (filter.getDocumentDateTo() != null) {
                sql.append(" AND a.fecha_documento <= :to ");
                params.addValue("to", filter.getDocumentDateTo());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND a.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(a.nombre_archivo) LIKE UPPER(:search)
                        OR UPPER(a.descripcion)  LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "a.created_at";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<AdjuntoClinicoTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
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

    // ── Mappers ──────────────────────────────────────────────────────────────

    private AdjuntoClinicoResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return AdjuntoClinicoResponseDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .encounterId(toLong(row.get("atencion_id")))
                .documentType((String) row.get("tipo_documento"))
                .fileName((String) row.get("nombre_archivo"))
                .description((String) row.get("descripcion"))
                .fileUrl((String) row.get("url_archivo"))
                .mimeType((String) row.get("mime_type"))
                .sizeBytes(toLong(row.get("tamano_bytes")))
                .uploadingProfessionalId(toLong(row.get("profesional_carga_id")))
                .uploadingProfessionalName((String) row.get("profesional_nombre"))
                .documentDate(toLocalDate(row.get("fecha_documento")))
                .isConfidential((Boolean) row.get("es_confidencial"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private AdjuntoClinicoTableDto mapRowToTableDto(Map<String, Object> row) {
        return AdjuntoClinicoTableDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .encounterId(toLong(row.get("atencion_id")))
                .documentType((String) row.get("tipo_documento"))
                .fileName((String) row.get("nombre_archivo"))
                .mimeType((String) row.get("mime_type"))
                .sizeBytes(toLong(row.get("tamano_bytes")))
                .documentDate(toLocalDate(row.get("fecha_documento")))
                .uploadingProfessionalName((String) row.get("profesional_nombre"))
                .isConfidential((Boolean) row.get("es_confidencial"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
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

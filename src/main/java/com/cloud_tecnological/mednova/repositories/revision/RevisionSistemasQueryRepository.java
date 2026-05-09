package com.cloud_tecnological.mednova.repositories.revision;

import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasFilterParams;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasResponseDto;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasTableDto;
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
public class RevisionSistemasQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RevisionSistemasQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-082: Lectura por ID ────────────────────────────────────────

    public Optional<RevisionSistemasResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT r.id,
                       r.atencion_id,
                       adm.paciente_id,
                       r.sistema,
                       r.sin_alteracion,
                       r.hallazgos,
                       r.activo,
                       r.created_at,
                       r.updated_at,
                       r.usuario_creacion
                FROM revision_sistemas r
                INNER JOIN atencion a   ON a.id   = r.atencion_id
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE r.id         = :id
                  AND r.empresa_id = :empresa_id
                  AND r.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public PageImpl<RevisionSistemasTableDto> listRevisiones(
            PageableDto<RevisionSistemasFilterParams> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        RevisionSistemasFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT r.id,
                       r.atencion_id,
                       adm.paciente_id,
                       r.sistema,
                       r.sin_alteracion,
                       r.hallazgos,
                       r.activo,
                       r.created_at,
                       COUNT(*) OVER()      AS total_rows
                FROM revision_sistemas r
                INNER JOIN atencion a   ON a.id   = r.atencion_id
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE r.empresa_id = :empresa_id
                  AND r.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (filter != null) {
            if (filter.getEncounterId() != null) {
                sql.append(" AND r.atencion_id = :atencion_id ");
                params.addValue("atencion_id", filter.getEncounterId());
            }
            if (filter.getPatientId() != null) {
                sql.append(" AND adm.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getSystem() != null && !filter.getSystem().isBlank()) {
                sql.append(" AND r.sistema = :sistema ");
                params.addValue("sistema", filter.getSystem().trim());
            }
            if (filter.getWithoutAlteration() != null) {
                sql.append(" AND r.sin_alteracion = :sin_alteracion ");
                params.addValue("sin_alteracion", filter.getWithoutAlteration());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND r.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(r.hallazgos) LIKE UPPER(:search)
                        OR UPPER(r.sistema) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "r.created_at";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<RevisionSistemasTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Validaciones cross-tenant y reglas ──────────────────────────────────

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

    /** Existe otra revision para la misma atencion y sistema (excluyendo el id dado). */
    public boolean reviewExistsForAtencionAndSystem(Long atencion_id, String sistema,
                                                    Long empresa_id, Long excludeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM revision_sistemas
                WHERE empresa_id  = :empresa_id
                  AND atencion_id = :atencion_id
                  AND sistema     = :sistema
                  AND deleted_at IS NULL
                """);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("atencion_id", atencion_id)
                .addValue("sistema", sistema);

        if (excludeId != null) {
            sql.append(" AND id <> :exclude_id ");
            params.addValue("exclude_id", excludeId);
        }

        Long count = jdbc.queryForObject(sql.toString(), params, Long.class);
        return count != null && count > 0;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private RevisionSistemasResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return RevisionSistemasResponseDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .system((String) row.get("sistema"))
                .withoutAlteration((Boolean) row.get("sin_alteracion"))
                .findings((String) row.get("hallazgos"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .build();
    }

    private RevisionSistemasTableDto mapRowToTableDto(Map<String, Object> row) {
        return RevisionSistemasTableDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .system((String) row.get("sistema"))
                .withoutAlteration((Boolean) row.get("sin_alteracion"))
                .findings((String) row.get("hallazgos"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
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

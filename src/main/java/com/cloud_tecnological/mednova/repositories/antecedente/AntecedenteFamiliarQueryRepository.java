package com.cloud_tecnological.mednova.repositories.antecedente;

import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarFilterParams;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarTableDto;
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
public class AntecedenteFamiliarQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AntecedenteFamiliarQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-080: Lectura de antecedente familiar por ID ────────────────

    public Optional<AntecedenteFamiliarResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT a.id,
                       a.paciente_id,
                       a.parentesco,
                       a.catalogo_diagnostico_id,
                       cd.codigo                AS diagnostico_codigo,
                       cd.nombre                AS diagnostico_nombre,
                       a.descripcion,
                       a.edad_aparicion,
                       a.es_fallecido,
                       a.causa_fallecimiento,
                       a.observaciones,
                       a.activo,
                       a.created_at,
                       a.updated_at,
                       a.usuario_creacion,
                       a.usuario_modificacion
                FROM antecedente_familiar a
                LEFT JOIN catalogo_diagnostico cd ON cd.id = a.catalogo_diagnostico_id
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

    public PageImpl<AntecedenteFamiliarTableDto> listAntecedentes(
            PageableDto<AntecedenteFamiliarFilterParams> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        AntecedenteFamiliarFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT a.id,
                       a.paciente_id,
                       a.parentesco,
                       cd.codigo            AS diagnostico_codigo,
                       cd.nombre            AS diagnostico_nombre,
                       a.descripcion,
                       a.edad_aparicion,
                       a.es_fallecido,
                       a.causa_fallecimiento,
                       a.activo,
                       COUNT(*) OVER()      AS total_rows
                FROM antecedente_familiar a
                LEFT JOIN catalogo_diagnostico cd ON cd.id = a.catalogo_diagnostico_id
                WHERE a.empresa_id = :empresa_id
                  AND a.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (filter != null) {
            if (filter.getPatientId() != null) {
                sql.append(" AND a.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getKinship() != null && !filter.getKinship().trim().isEmpty()) {
                sql.append(" AND UPPER(a.parentesco) = UPPER(:parentesco) ");
                params.addValue("parentesco", filter.getKinship().trim());
            }
            if (Boolean.TRUE.equals(filter.getOnlyDeceased())) {
                sql.append(" AND a.es_fallecido = true ");
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND a.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(a.descripcion)         LIKE UPPER(:search)
                        OR UPPER(a.parentesco)       LIKE UPPER(:search)
                        OR UPPER(a.causa_fallecimiento) LIKE UPPER(:search)
                        OR UPPER(cd.codigo)          LIKE UPPER(:search)
                        OR UPPER(cd.nombre)          LIKE UPPER(:search)
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
        List<AntecedenteFamiliarTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Validaciones cross-tenant (lecturas auxiliares) ─────────────────────

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

    public boolean catalogoDiagnosticoExists(Long catalogo_id) {
        String sql = "SELECT COUNT(*) FROM catalogo_diagnostico WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", catalogo_id), Long.class);
        return count != null && count > 0;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private AntecedenteFamiliarResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return AntecedenteFamiliarResponseDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .kinship((String) row.get("parentesco"))
                .catalogDiagnosisId(toLong(row.get("catalogo_diagnostico_id")))
                .catalogDiagnosisCode((String) row.get("diagnostico_codigo"))
                .catalogDiagnosisName((String) row.get("diagnostico_nombre"))
                .description((String) row.get("descripcion"))
                .ageOfOnset(toInteger(row.get("edad_aparicion")))
                .isDeceased((Boolean) row.get("es_fallecido"))
                .causeOfDeath((String) row.get("causa_fallecimiento"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private AntecedenteFamiliarTableDto mapRowToTableDto(Map<String, Object> row) {
        return AntecedenteFamiliarTableDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .kinship((String) row.get("parentesco"))
                .catalogDiagnosisCode((String) row.get("diagnostico_codigo"))
                .catalogDiagnosisName((String) row.get("diagnostico_nombre"))
                .description((String) row.get("descripcion"))
                .ageOfOnset(toInteger(row.get("edad_aparicion")))
                .isDeceased((Boolean) row.get("es_fallecido"))
                .causeOfDeath((String) row.get("causa_fallecimiento"))
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
}

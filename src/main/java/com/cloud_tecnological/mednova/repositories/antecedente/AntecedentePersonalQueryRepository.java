package com.cloud_tecnological.mednova.repositories.antecedente;

import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalFilterParams;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalTableDto;
import com.cloud_tecnological.mednova.dto.antecedente.TipoAntecedenteResponseDto;
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
public class AntecedentePersonalQueryRepository {

    private static final String CODIGO_ALERGICO = "ALERGICO";

    private final NamedParameterJdbcTemplate jdbc;

    public AntecedentePersonalQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-079: Lectura de antecedente personal por ID ────────────────

    public Optional<AntecedentePersonalResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT a.id,
                       a.paciente_id,
                       a.tipo_antecedente_id,
                       t.codigo                       AS tipo_codigo,
                       t.nombre                       AS tipo_nombre,
                       a.catalogo_diagnostico_id,
                       cd.codigo                      AS diagnostico_codigo,
                       cd.nombre                      AS diagnostico_nombre,
                       a.descripcion,
                       a.fecha_inicio,
                       a.fecha_fin,
                       a.es_activo,
                       a.severidad,
                       a.observaciones,
                       a.profesional_registro_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       a.activo,
                       a.created_at,
                       a.updated_at,
                       a.usuario_creacion,
                       a.usuario_modificacion
                FROM antecedente_personal a
                INNER JOIN tipo_antecedente       t  ON t.id  = a.tipo_antecedente_id
                LEFT  JOIN catalogo_diagnostico   cd ON cd.id = a.catalogo_diagnostico_id
                LEFT  JOIN profesional_salud      ps ON ps.id = a.profesional_registro_id
                LEFT  JOIN tercero                te ON te.id = ps.tercero_id
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

    public PageImpl<AntecedentePersonalTableDto> listAntecedentes(
            PageableDto<AntecedentePersonalFilterParams> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        AntecedentePersonalFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT a.id,
                       a.paciente_id,
                       t.codigo            AS tipo_codigo,
                       t.nombre            AS tipo_nombre,
                       cd.codigo           AS diagnostico_codigo,
                       cd.nombre           AS diagnostico_nombre,
                       a.descripcion,
                       a.severidad,
                       a.fecha_inicio,
                       a.fecha_fin,
                       a.es_activo,
                       a.activo,
                       COUNT(*) OVER()     AS total_rows
                FROM antecedente_personal a
                INNER JOIN tipo_antecedente     t  ON t.id  = a.tipo_antecedente_id
                LEFT  JOIN catalogo_diagnostico cd ON cd.id = a.catalogo_diagnostico_id
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
            if (filter.getAntecedentTypeId() != null) {
                sql.append(" AND a.tipo_antecedente_id = :tipo_id ");
                params.addValue("tipo_id", filter.getAntecedentTypeId());
            }
            if (Boolean.TRUE.equals(filter.getOnlyAllergies())) {
                sql.append(" AND t.codigo = :codigo_alergico ");
                params.addValue("codigo_alergico", CODIGO_ALERGICO);
            }
            if (Boolean.TRUE.equals(filter.getOnlyActiveCondition())) {
                sql.append(" AND a.es_activo = true ");
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND a.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(a.descripcion)  LIKE UPPER(:search)
                        OR UPPER(t.nombre)    LIKE UPPER(:search)
                        OR UPPER(t.codigo)    LIKE UPPER(:search)
                        OR UPPER(cd.codigo)   LIKE UPPER(:search)
                        OR UPPER(cd.nombre)   LIKE UPPER(:search)
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
        List<AntecedentePersonalTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
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

    public boolean tipoAntecedenteActivoExists(Long tipo_id) {
        String sql = """
                SELECT COUNT(*)
                FROM tipo_antecedente
                WHERE id         = :id
                  AND activo     = true
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", tipo_id), Long.class);
        return count != null && count > 0;
    }

    public boolean catalogoDiagnosticoExists(Long catalogo_id) {
        String sql = "SELECT COUNT(*) FROM catalogo_diagnostico WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", catalogo_id), Long.class);
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

    public List<TipoAntecedenteResponseDto> listTiposAntecedente() {
        String sql = """
                SELECT id, codigo, nombre, descripcion, activo
                FROM tipo_antecedente
                WHERE deleted_at IS NULL
                  AND activo     = true
                ORDER BY nombre ASC
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource(), new ColumnMapRowMapper());
        return rows.stream().map(this::mapRowToTipoAntecedenteDto).toList();
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private AntecedentePersonalResponseDto mapRowToResponseDto(Map<String, Object> row) {
        String tipoCodigo = (String) row.get("tipo_codigo");
        return AntecedentePersonalResponseDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .antecedentTypeId(toLong(row.get("tipo_antecedente_id")))
                .antecedentTypeCode(tipoCodigo)
                .antecedentTypeName((String) row.get("tipo_nombre"))
                .catalogDiagnosisId(toLong(row.get("catalogo_diagnostico_id")))
                .catalogDiagnosisCode((String) row.get("diagnostico_codigo"))
                .catalogDiagnosisName((String) row.get("diagnostico_nombre"))
                .description((String) row.get("descripcion"))
                .startDate(toLocalDate(row.get("fecha_inicio")))
                .endDate(toLocalDate(row.get("fecha_fin")))
                .isActiveCondition((Boolean) row.get("es_activo"))
                .isAllergy(CODIGO_ALERGICO.equals(tipoCodigo))
                .severity((String) row.get("severidad"))
                .observations((String) row.get("observaciones"))
                .registeringProfessionalId(toLong(row.get("profesional_registro_id")))
                .registeringProfessionalName((String) row.get("profesional_nombre"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private AntecedentePersonalTableDto mapRowToTableDto(Map<String, Object> row) {
        String tipoCodigo = (String) row.get("tipo_codigo");
        return AntecedentePersonalTableDto.builder()
                .id(toLong(row.get("id")))
                .patientId(toLong(row.get("paciente_id")))
                .antecedentTypeCode(tipoCodigo)
                .antecedentTypeName((String) row.get("tipo_nombre"))
                .isAllergy(CODIGO_ALERGICO.equals(tipoCodigo))
                .catalogDiagnosisCode((String) row.get("diagnostico_codigo"))
                .catalogDiagnosisName((String) row.get("diagnostico_nombre"))
                .description((String) row.get("descripcion"))
                .severity((String) row.get("severidad"))
                .startDate(toLocalDate(row.get("fecha_inicio")))
                .endDate(toLocalDate(row.get("fecha_fin")))
                .isActiveCondition((Boolean) row.get("es_activo"))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private TipoAntecedenteResponseDto mapRowToTipoAntecedenteDto(Map<String, Object> row) {
        return TipoAntecedenteResponseDto.builder()
                .id(toLong(row.get("id")))
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .description((String) row.get("descripcion"))
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

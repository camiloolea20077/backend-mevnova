package com.cloud_tecnological.mednova.repositories.paciente;

import com.cloud_tecnological.mednova.dto.paciente.PacienteResponseDto;
import com.cloud_tecnological.mednova.dto.paciente.PacienteTableDto;
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
public class PacienteQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PacienteQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<PacienteResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT p.id,
                       p.empresa_id,
                       p.tercero_id,
                       t.nombre_completo,
                       t.numero_documento,
                       td.codigo         AS tipo_documento_codigo,
                       t.fecha_nacimiento,
                       p.grupo_sanguineo_id,
                       gs.nombre         AS grupo_sanguineo_nombre,
                       p.factor_rh_id,
                       rh.nombre         AS factor_rh_nombre,
                       p.discapacidad_id,
                       p.grupo_atencion_id,
                       p.alergias_conocidas,
                       p.observaciones_clinicas,
                       p.activo,
                       p.created_at
                FROM paciente p
                INNER JOIN tercero t        ON t.id  = p.tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                LEFT  JOIN grupo_sanguineo gs ON gs.id = p.grupo_sanguineo_id
                LEFT  JOIN factor_rh rh       ON rh.id = p.factor_rh_id
                WHERE p.id = :id
                  AND p.empresa_id = :empresa_id
                  AND p.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public Optional<PacienteResponseDto> findActiveByTercero(Long tercero_id, Long empresa_id) {
        String sql = """
                SELECT p.id,
                       p.empresa_id,
                       p.tercero_id,
                       t.nombre_completo,
                       t.numero_documento,
                       td.codigo          AS tipo_documento_codigo,
                       t.fecha_nacimiento,
                       p.grupo_sanguineo_id,
                       gs.nombre          AS grupo_sanguineo_nombre,
                       p.factor_rh_id,
                       rh.nombre          AS factor_rh_nombre,
                       p.discapacidad_id,
                       p.grupo_atencion_id,
                       p.alergias_conocidas,
                       p.observaciones_clinicas,
                       p.activo,
                       p.created_at
                FROM paciente p
                INNER JOIN tercero t        ON t.id  = p.tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                LEFT  JOIN grupo_sanguineo gs ON gs.id = p.grupo_sanguineo_id
                LEFT  JOIN factor_rh rh       ON rh.id = p.factor_rh_id
                WHERE p.tercero_id = :tercero_id
                  AND p.empresa_id = :empresa_id
                  AND p.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public boolean existsByTercero(Long tercero_id, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM paciente
                WHERE tercero_id = :tercero_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("tercero_id", tercero_id)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<PacienteTableDto> listPacientes(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT p.id,
                       p.tercero_id,
                       t.nombre_completo,
                       td.codigo           AS tipo_documento_codigo,
                       t.numero_documento,
                       t.fecha_nacimiento,
                       gs.nombre           AS grupo_sanguineo_nombre,
                       rh.nombre           AS factor_rh_nombre,
                       p.activo,
                       COUNT(*) OVER()     AS total_rows
                FROM paciente p
                INNER JOIN tercero t        ON t.id  = p.tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                LEFT  JOIN grupo_sanguineo gs ON gs.id = p.grupo_sanguineo_id
                LEFT  JOIN factor_rh rh       ON rh.id = p.factor_rh_id
                WHERE p.empresa_id = :empresa_id
                  AND p.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        unaccent(UPPER(t.nombre_completo)) LIKE unaccent(UPPER(:search))
                        OR t.numero_documento = :search_exact
                    )
                    """);
            params.addValue("search", "%" + search + "%");
            params.addValue("search_exact", search);
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "t.nombre_completo";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<PacienteTableDto> dtos = result.stream().map(this::mapToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private PacienteResponseDto mapToResponseDto(Map<String, Object> row) {
        return PacienteResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .fullName((String) row.get("nombre_completo"))
                .documentNumber((String) row.get("numero_documento"))
                .documentTypeCode((String) row.get("tipo_documento_codigo"))
                .birthDate(toLocalDate(row.get("fecha_nacimiento")))
                .bloodGroupId(toLong(row.get("grupo_sanguineo_id")))
                .bloodGroupName((String) row.get("grupo_sanguineo_nombre"))
                .rhFactorId(toLong(row.get("factor_rh_id")))
                .rhFactorName((String) row.get("factor_rh_nombre"))
                .disabilityId(toLong(row.get("discapacidad_id")))
                .attentionGroupId(toLong(row.get("grupo_atencion_id")))
                .knownAllergies((String) row.get("alergias_conocidas"))
                .clinicalObservations((String) row.get("observaciones_clinicas"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private PacienteTableDto mapToTableDto(Map<String, Object> row) {
        return PacienteTableDto.builder()
                .id(toLong(row.get("id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .fullName((String) row.get("nombre_completo"))
                .documentTypeCode((String) row.get("tipo_documento_codigo"))
                .documentNumber((String) row.get("numero_documento"))
                .birthDate(toLocalDate(row.get("fecha_nacimiento")))
                .bloodGroupName((String) row.get("grupo_sanguineo_nombre"))
                .rhFactorName((String) row.get("factor_rh_nombre"))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        return ((Number) v).longValue();
    }

    private LocalDate toLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        if (v instanceof Date d) return d.toLocalDate();
        return null;
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

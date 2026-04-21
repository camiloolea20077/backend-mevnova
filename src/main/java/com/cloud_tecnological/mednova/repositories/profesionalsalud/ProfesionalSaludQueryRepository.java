package com.cloud_tecnological.mednova.repositories.profesionalsalud;

import com.cloud_tecnological.mednova.dto.profesionalsalud.ProfesionalSaludResponseDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.ProfesionalSaludTableDto;
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
public class ProfesionalSaludQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ProfesionalSaludQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ProfesionalSaludResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT ps.id,
                       ps.empresa_id,
                       ps.tercero_id,
                       t.nombre_completo,
                       t.numero_documento,
                       td.codigo           AS tipo_documento_codigo,
                       ps.numero_registro_medico,
                       ps.especialidad_principal_id,
                       e.nombre            AS especialidad_nombre,
                       ps.fecha_ingreso,
                       ps.fecha_retiro,
                       ps.observaciones,
                       ps.activo,
                       ps.created_at
                FROM profesional_salud ps
                INNER JOIN tercero t        ON t.id  = ps.tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                LEFT  JOIN especialidad e   ON e.id  = ps.especialidad_principal_id
                WHERE ps.id = :id
                  AND ps.empresa_id = :empresa_id
                  AND ps.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public Optional<ProfesionalSaludResponseDto> findActiveByTercero(Long tercero_id, Long empresa_id) {
        String sql = """
                SELECT ps.id,
                       ps.empresa_id,
                       ps.tercero_id,
                       t.nombre_completo,
                       t.numero_documento,
                       td.codigo           AS tipo_documento_codigo,
                       ps.numero_registro_medico,
                       ps.especialidad_principal_id,
                       e.nombre            AS especialidad_nombre,
                       ps.fecha_ingreso,
                       ps.fecha_retiro,
                       ps.observaciones,
                       ps.activo,
                       ps.created_at
                FROM profesional_salud ps
                INNER JOIN tercero t        ON t.id  = ps.tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                LEFT  JOIN especialidad e   ON e.id  = ps.especialidad_principal_id
                WHERE ps.tercero_id = :tercero_id
                  AND ps.empresa_id = :empresa_id
                  AND ps.deleted_at IS NULL
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
                FROM profesional_salud
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

    public boolean existsByRegistroMedico(String registro, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM profesional_salud
                WHERE numero_registro_medico = :registro
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("registro", registro)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<ProfesionalSaludTableDto> listProfesionales(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT ps.id,
                       ps.tercero_id,
                       t.nombre_completo,
                       td.codigo           AS tipo_documento_codigo,
                       t.numero_documento,
                       ps.numero_registro_medico,
                       e.nombre            AS especialidad_nombre,
                       ps.activo,
                       COUNT(*) OVER()     AS total_rows
                FROM profesional_salud ps
                INNER JOIN tercero t        ON t.id  = ps.tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                LEFT  JOIN especialidad e   ON e.id  = ps.especialidad_principal_id
                WHERE ps.empresa_id = :empresa_id
                  AND ps.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        unaccent(UPPER(t.nombre_completo)) LIKE unaccent(UPPER(:search))
                        OR t.numero_documento = :search_exact
                        OR UPPER(ps.numero_registro_medico) LIKE UPPER(:search)
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
        List<ProfesionalSaludTableDto> dtos = result.stream().map(this::mapToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private ProfesionalSaludResponseDto mapToResponseDto(Map<String, Object> row) {
        return ProfesionalSaludResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .fullName((String) row.get("nombre_completo"))
                .documentNumber((String) row.get("numero_documento"))
                .documentTypeCode((String) row.get("tipo_documento_codigo"))
                .medicalRegistrationNumber((String) row.get("numero_registro_medico"))
                .primarySpecialtyId(toLong(row.get("especialidad_principal_id")))
                .primarySpecialtyName((String) row.get("especialidad_nombre"))
                .startDate(toLocalDate(row.get("fecha_ingreso")))
                .retirementDate(toLocalDate(row.get("fecha_retiro")))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private ProfesionalSaludTableDto mapToTableDto(Map<String, Object> row) {
        return ProfesionalSaludTableDto.builder()
                .id(toLong(row.get("id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .fullName((String) row.get("nombre_completo"))
                .documentTypeCode((String) row.get("tipo_documento_codigo"))
                .documentNumber((String) row.get("numero_documento"))
                .medicalRegistrationNumber((String) row.get("numero_registro_medico"))
                .primarySpecialtyName((String) row.get("especialidad_nombre"))
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

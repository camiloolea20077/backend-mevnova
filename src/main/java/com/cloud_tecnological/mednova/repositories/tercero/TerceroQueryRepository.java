package com.cloud_tecnological.mednova.repositories.tercero;

import com.cloud_tecnological.mednova.dto.tercero.TerceroResponseDto;
import com.cloud_tecnological.mednova.dto.tercero.TerceroTableDto;
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
public class TerceroQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TerceroQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<TerceroResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT t.id,
                       t.empresa_id,
                       t.tipo_tercero_id,
                       tt.nombre        AS tipo_tercero_nombre,
                       t.tipo_documento_id,
                       td.codigo        AS tipo_documento_codigo,
                       td.nombre        AS tipo_documento_nombre,
                       t.numero_documento,
                       t.digito_verificacion,
                       t.primer_nombre,
                       t.segundo_nombre,
                       t.primer_apellido,
                       t.segundo_apellido,
                       t.razon_social,
                       t.nombre_completo,
                       t.fecha_nacimiento,
                       t.sexo_id,
                       t.genero_id,
                       t.identidad_genero_id,
                       t.orientacion_sexual_id,
                       t.estado_civil_id,
                       t.nivel_escolaridad_id,
                       t.ocupacion_id,
                       t.pertenencia_etnica_id,
                       t.pais_nacimiento_id,
                       t.municipio_nacimiento_id,
                       t.observaciones,
                       t.activo,
                       t.created_at
                FROM tercero t
                INNER JOIN tipo_tercero tt ON tt.id = t.tipo_tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                WHERE t.id = :id
                  AND t.empresa_id = :empresa_id
                  AND t.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public Optional<TerceroResponseDto> findByDocument(Long tipo_documento_id, String numero_documento, Long empresa_id) {
        String sql = """
                SELECT t.id,
                       t.empresa_id,
                       t.tipo_tercero_id,
                       tt.nombre        AS tipo_tercero_nombre,
                       t.tipo_documento_id,
                       td.codigo        AS tipo_documento_codigo,
                       td.nombre        AS tipo_documento_nombre,
                       t.numero_documento,
                       t.digito_verificacion,
                       t.primer_nombre,
                       t.segundo_nombre,
                       t.primer_apellido,
                       t.segundo_apellido,
                       t.razon_social,
                       t.nombre_completo,
                       t.fecha_nacimiento,
                       t.sexo_id,
                       t.genero_id,
                       t.identidad_genero_id,
                       t.orientacion_sexual_id,
                       t.estado_civil_id,
                       t.nivel_escolaridad_id,
                       t.ocupacion_id,
                       t.pertenencia_etnica_id,
                       t.pais_nacimiento_id,
                       t.municipio_nacimiento_id,
                       t.observaciones,
                       t.activo,
                       t.created_at
                FROM tercero t
                INNER JOIN tipo_tercero tt ON tt.id = t.tipo_tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                WHERE t.tipo_documento_id = :tipo_documento_id
                  AND t.numero_documento  = :numero_documento
                  AND t.empresa_id        = :empresa_id
                  AND t.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tipo_documento_id", tipo_documento_id)
                .addValue("numero_documento", numero_documento)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public boolean existsByDocumento(Long tipo_documento_id, String numero_documento, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM tercero
                WHERE tipo_documento_id = :tipo_documento_id
                  AND numero_documento  = :numero_documento
                  AND empresa_id        = :empresa_id
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tipo_documento_id", tipo_documento_id)
                .addValue("numero_documento", numero_documento)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public PageImpl<TerceroTableDto> listTerceros(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT t.id,
                       td.codigo        AS tipo_documento_codigo,
                       t.numero_documento,
                       t.nombre_completo,
                       t.fecha_nacimiento,
                       tt.nombre        AS tipo_tercero_nombre,
                       t.activo,
                       COUNT(*) OVER()  AS total_rows
                FROM tercero t
                INNER JOIN tipo_tercero tt  ON tt.id = t.tipo_tercero_id
                INNER JOIN tipo_documento td ON td.id = t.tipo_documento_id
                WHERE t.empresa_id  = :empresa_id
                  AND t.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        unaccent(UPPER(t.nombre_completo))  LIKE unaccent(UPPER(:search))
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
        List<TerceroTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private TerceroResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return TerceroResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .thirdPartyTypeId(toLong(row.get("tipo_tercero_id")))
                .thirdPartyTypeName((String) row.get("tipo_tercero_nombre"))
                .documentTypeId(toLong(row.get("tipo_documento_id")))
                .documentTypeCode((String) row.get("tipo_documento_codigo"))
                .documentTypeName((String) row.get("tipo_documento_nombre"))
                .documentNumber((String) row.get("numero_documento"))
                .verificationDigit((String) row.get("digito_verificacion"))
                .firstName((String) row.get("primer_nombre"))
                .secondName((String) row.get("segundo_nombre"))
                .firstLastName((String) row.get("primer_apellido"))
                .secondLastName((String) row.get("segundo_apellido"))
                .companyName((String) row.get("razon_social"))
                .fullName((String) row.get("nombre_completo"))
                .birthDate(toLocalDate(row.get("fecha_nacimiento")))
                .sexId(toLong(row.get("sexo_id")))
                .genderId(toLong(row.get("genero_id")))
                .genderIdentityId(toLong(row.get("identidad_genero_id")))
                .sexualOrientationId(toLong(row.get("orientacion_sexual_id")))
                .maritalStatusId(toLong(row.get("estado_civil_id")))
                .educationLevelId(toLong(row.get("nivel_escolaridad_id")))
                .occupationId(toLong(row.get("ocupacion_id")))
                .ethnicGroupId(toLong(row.get("pertenencia_etnica_id")))
                .birthCountryId(toLong(row.get("pais_nacimiento_id")))
                .birthMunicipalityId(toLong(row.get("municipio_nacimiento_id")))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private TerceroTableDto mapRowToTableDto(Map<String, Object> row) {
        return TerceroTableDto.builder()
                .id(toLong(row.get("id")))
                .documentTypeCode((String) row.get("tipo_documento_codigo"))
                .documentNumber((String) row.get("numero_documento"))
                .fullName((String) row.get("nombre_completo"))
                .birthDate(toLocalDate(row.get("fecha_nacimiento")))
                .thirdPartyTypeName((String) row.get("tipo_tercero_nombre"))
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

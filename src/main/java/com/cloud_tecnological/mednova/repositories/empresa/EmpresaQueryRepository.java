package com.cloud_tecnological.mednova.repositories.empresa;

import com.cloud_tecnological.mednova.dto.empresa.EmpresaResponseDto;
import com.cloud_tecnological.mednova.dto.empresa.EmpresaTableDto;
import com.cloud_tecnological.mednova.entity.EmpresaEntity;
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
public class EmpresaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EmpresaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<EmpresaEntity> findActiveByCodigo(String codigo) {
        String sql = """
                SELECT id, codigo, nit, digito_verificacion, razon_social, nombre_comercial,
                       representante_legal, telefono, correo, pais_id, departamento_id,
                       municipio_id, direccion, logo_url, observaciones, activo
                FROM empresa
                WHERE codigo = :codigo
                  AND activo = true
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToEntity(rows.get(0)));
    }

    public Optional<EmpresaEntity> findActiveByNit(String nit) {
        String sql = """
                SELECT id, codigo, nit, digito_verificacion, razon_social, nombre_comercial,
                       representante_legal, telefono, correo, pais_id, departamento_id,
                       municipio_id, direccion, logo_url, observaciones, activo
                FROM empresa
                WHERE nit = :nit
                  AND activo = true
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("nit", nit), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToEntity(rows.get(0)));
    }

    public Optional<EmpresaResponseDto> findActiveById(Long id) {
        String sql = """
                SELECT id, codigo, nit, digito_verificacion, razon_social, nombre_comercial,
                       representante_legal, telefono, correo, pais_id, departamento_id,
                       municipio_id, direccion, logo_url, observaciones, activo, created_at
                FROM empresa
                WHERE id = :id
                  AND deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("id", id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public boolean existsByCodigoExcludingId(String codigo, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM empresa
                WHERE codigo = :codigo
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("codigo", codigo).addValue("exclude_id", excludeId == null ? -1 : excludeId),
                Long.class);
        return count != null && count > 0;
    }

    public boolean existsByNitExcludingId(String nit, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM empresa
                WHERE nit = :nit
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql,
                new MapSqlParameterSource("nit", nit).addValue("exclude_id", excludeId == null ? -1 : excludeId),
                Long.class);
        return count != null && count > 0;
    }

    public PageImpl<EmpresaTableDto> findAll(PageableDto<?> pageable) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT id, codigo, nit, razon_social, nombre_comercial,
                       telefono, correo, activo,
                       COUNT(*) OVER() AS total_rows
                FROM empresa
                WHERE deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(razon_social) LIKE UPPER(:search)
                        OR UPPER(nombre_comercial) LIKE UPPER(:search)
                        OR nit LIKE :search
                        OR codigo LIKE :search
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "razon_social";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<EmpresaTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private EmpresaEntity mapRowToEntity(Map<String, Object> row) {
        EmpresaEntity e = new EmpresaEntity();
        e.setId(toLong(row.get("id")));
        e.setCodigo((String) row.get("codigo"));
        e.setNit((String) row.get("nit"));
        e.setDigito_verificacion((String) row.get("digito_verificacion"));
        e.setRazon_social((String) row.get("razon_social"));
        e.setNombre_comercial((String) row.get("nombre_comercial"));
        e.setRepresentante_legal((String) row.get("representante_legal"));
        e.setTelefono((String) row.get("telefono"));
        e.setCorreo((String) row.get("correo"));
        e.setPais_id(toLong(row.get("pais_id")));
        e.setDepartamento_id(toLong(row.get("departamento_id")));
        e.setMunicipio_id(toLong(row.get("municipio_id")));
        e.setDireccion((String) row.get("direccion"));
        e.setLogo_url((String) row.get("logo_url"));
        e.setObservaciones((String) row.get("observaciones"));
        e.setActivo((Boolean) row.get("activo"));
        return e;
    }

    private EmpresaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return EmpresaResponseDto.builder()
                .id(toLong(row.get("id")))
                .code((String) row.get("codigo"))
                .nit((String) row.get("nit"))
                .verificationDigit((String) row.get("digito_verificacion"))
                .businessName((String) row.get("razon_social"))
                .tradeName((String) row.get("nombre_comercial"))
                .legalRepresentative((String) row.get("representante_legal"))
                .phone((String) row.get("telefono"))
                .email((String) row.get("correo"))
                .countryId(toLong(row.get("pais_id")))
                .departmentId(toLong(row.get("departamento_id")))
                .municipalityId(toLong(row.get("municipio_id")))
                .address((String) row.get("direccion"))
                .logoUrl((String) row.get("logo_url"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private EmpresaTableDto mapRowToTableDto(Map<String, Object> row) {
        return EmpresaTableDto.builder()
                .id(toLong(row.get("id")))
                .code((String) row.get("codigo"))
                .nit((String) row.get("nit"))
                .businessName((String) row.get("razon_social"))
                .tradeName((String) row.get("nombre_comercial"))
                .phone((String) row.get("telefono"))
                .email((String) row.get("correo"))
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

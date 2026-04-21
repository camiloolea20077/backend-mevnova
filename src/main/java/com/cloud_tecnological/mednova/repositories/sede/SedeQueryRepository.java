package com.cloud_tecnological.mednova.repositories.sede;

import com.cloud_tecnological.mednova.dto.auth.SedeDto;
import com.cloud_tecnological.mednova.dto.sede.SedeResponseDto;
import com.cloud_tecnological.mednova.dto.sede.SedeTableDto;
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
public class SedeQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SedeQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── Auth queries (existentes) ────────────────────────────────────────────

    public List<SedeDto> findAvailableByUserAndEmpresa(Long usuarioId, Long empresaId) {
        String sql = """
                SELECT DISTINCT s.id, s.codigo, s.nombre
                FROM usuario_rol ur
                INNER JOIN sede s ON s.id = ur.sede_id
                    AND s.activo = true
                    AND s.deleted_at IS NULL
                WHERE ur.usuario_id = :usuario_id
                  AND ur.empresa_id = :empresa_id
                  AND ur.activo = true
                  AND ur.deleted_at IS NULL
                ORDER BY s.nombre ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("usuario_id", usuarioId)
                .addValue("empresa_id", empresaId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return rows.stream()
                .map(row -> SedeDto.builder()
                        .id(((Number) row.get("id")).longValue())
                        .codigo((String) row.get("codigo"))
                        .nombre((String) row.get("nombre"))
                        .build())
                .toList();
    }

    public boolean existsActiveByIdAndEmpresa(Long sedeId, Long empresaId) {
        String sql = """
                SELECT COUNT(*)
                FROM sede
                WHERE id = :sede_id
                  AND empresa_id = :empresa_id
                  AND activo = true
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sede_id", sedeId)
                .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    // ── HU-FASE1-002: Gestión de sedes ──────────────────────────────────────

    public Optional<SedeResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT s.id,
                       s.codigo,
                       s.codigo_habilitacion_reps,
                       s.nombre,
                       s.pais_id,
                       s.departamento_id,
                       s.municipio_id,
                       m.nombre AS municipio_nombre,
                       s.direccion,
                       s.telefono,
                       s.correo,
                       s.es_principal,
                       s.activo,
                       s.created_at
                FROM sede s
                LEFT JOIN municipio m ON m.id = s.municipio_id
                WHERE s.id = :id
                  AND s.empresa_id = :empresa_id
                  AND s.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public boolean existsByCodigoAndEmpresa(String codigo, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM sede
                WHERE codigo = :codigo
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("codigo", codigo)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public void unmarkPrincipalByEmpresa(Long empresa_id) {
        String sql = """
                UPDATE sede
                SET es_principal = false,
                    updated_at   = NOW()
                WHERE empresa_id = :empresa_id
                  AND es_principal = true
                  AND deleted_at IS NULL
                """;
        jdbc.update(sql, new MapSqlParameterSource("empresa_id", empresa_id));
    }

    public PageImpl<SedeTableDto> listSedes(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT s.id,
                       s.codigo,
                       s.nombre,
                       m.nombre AS municipio_nombre,
                       s.es_principal,
                       s.activo,
                       COUNT(*) OVER() AS total_rows
                FROM sede s
                LEFT JOIN municipio m ON m.id = s.municipio_id
                WHERE s.empresa_id = :empresa_id
                  AND s.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(s.nombre) LIKE UPPER(:search)
                        OR UPPER(s.codigo) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "s.nombre";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<SedeTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private SedeResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return SedeResponseDto.builder()
                .id(toLong(row.get("id")))
                .code((String) row.get("codigo"))
                .repsCode((String) row.get("codigo_habilitacion_reps"))
                .name((String) row.get("nombre"))
                .countryId(toLong(row.get("pais_id")))
                .departmentId(toLong(row.get("departamento_id")))
                .municipalityId(toLong(row.get("municipio_id")))
                .municipalityName((String) row.get("municipio_nombre"))
                .address((String) row.get("direccion"))
                .phone((String) row.get("telefono"))
                .email((String) row.get("correo"))
                .isPrincipal((Boolean) row.get("es_principal"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private SedeTableDto mapRowToTableDto(Map<String, Object> row) {
        return SedeTableDto.builder()
                .id(toLong(row.get("id")))
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .municipalityName((String) row.get("municipio_nombre"))
                .isPrincipal((Boolean) row.get("es_principal"))
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

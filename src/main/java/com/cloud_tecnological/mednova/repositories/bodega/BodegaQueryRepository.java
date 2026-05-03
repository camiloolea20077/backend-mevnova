package com.cloud_tecnological.mednova.repositories.bodega;

import com.cloud_tecnological.mednova.dto.bodega.BodegaResponseDto;
import com.cloud_tecnological.mednova.dto.bodega.BodegaTableDto;
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
public class BodegaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public BodegaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-067: Gestión de bodegas ────────────────────────────────────

    public Optional<BodegaResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT b.id,
                       b.sede_id,
                       s.nombre AS sede_nombre,
                       b.codigo,
                       b.nombre,
                       b.tipo_bodega,
                       b.responsable_id,
                       t.nombre_completo AS responsable_nombre,
                       b.ubicacion_fisica,
                       b.es_principal,
                       b.permite_dispensar,
                       b.permite_recibir,
                       b.observaciones,
                       b.activo,
                       b.created_at
                FROM bodega b
                INNER JOIN sede s ON s.id = b.sede_id
                LEFT JOIN profesional_salud p ON p.id = b.responsable_id
                LEFT JOIN tercero t ON t.id = p.tercero_id
                WHERE b.id = :id
                  AND b.empresa_id = :empresa_id
                  AND b.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToResponseDto(rows.get(0)));
    }

    public boolean existsByCodigoAndSede(String codigo, Long sede_id, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM bodega
                WHERE codigo = :codigo
                  AND sede_id = :sede_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id <> :exclude_id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("codigo", codigo)
                .addValue("sede_id", sede_id)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1L : excludeId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsActiveSedeByIdAndEmpresa(Long sede_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM sede
                WHERE id = :sede_id
                  AND empresa_id = :empresa_id
                  AND activo = true
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sede_id", sede_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsProfesionalByIdAndEmpresa(Long profesional_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM profesional_salud
                WHERE id = :id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", profesional_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public void unmarkPrincipalBySede(Long sede_id, Long empresa_id) {
        String sql = """
                UPDATE bodega
                SET es_principal = false,
                    updated_at   = NOW()
                WHERE sede_id    = :sede_id
                  AND empresa_id = :empresa_id
                  AND es_principal = true
                  AND deleted_at IS NULL
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("sede_id", sede_id)
                .addValue("empresa_id", empresa_id);
        jdbc.update(sql, params);
    }

    public boolean hasStock(Long bodega_id, Long empresa_id) {
        String sql = """
                SELECT COUNT(*)
                FROM stock_lote
                WHERE bodega_id  = :bodega_id
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND cantidad_total > 0
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("bodega_id", bodega_id)
                .addValue("empresa_id", empresa_id);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public PageImpl<BodegaTableDto> listBodegas(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT b.id,
                       s.nombre AS sede_nombre,
                       b.codigo,
                       b.nombre,
                       b.tipo_bodega,
                       t.nombre_completo AS responsable_nombre,
                       b.es_principal,
                       b.permite_dispensar,
                       b.permite_recibir,
                       b.activo,
                       COUNT(*) OVER() AS total_rows
                FROM bodega b
                INNER JOIN sede s ON s.id = b.sede_id
                LEFT JOIN profesional_salud p ON p.id = b.responsable_id
                LEFT JOIN tercero t ON t.id = p.tercero_id
                WHERE b.empresa_id = :empresa_id
                  AND b.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                    AND (
                        UPPER(b.nombre) LIKE UPPER(:search)
                        OR UPPER(b.codigo) LIKE UPPER(:search)
                        OR UPPER(s.nombre) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "b.nombre";
        String order   = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<BodegaTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private BodegaResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return BodegaResponseDto.builder()
                .id(toLong(row.get("id")))
                .branchId(toLong(row.get("sede_id")))
                .branchName((String) row.get("sede_nombre"))
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .warehouseType((String) row.get("tipo_bodega"))
                .responsibleId(toLong(row.get("responsable_id")))
                .responsibleName((String) row.get("responsable_nombre"))
                .physicalLocation((String) row.get("ubicacion_fisica"))
                .isPrincipal((Boolean) row.get("es_principal"))
                .allowsDispense((Boolean) row.get("permite_dispensar"))
                .allowsReceive((Boolean) row.get("permite_recibir"))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private BodegaTableDto mapRowToTableDto(Map<String, Object> row) {
        return BodegaTableDto.builder()
                .id(toLong(row.get("id")))
                .branchName((String) row.get("sede_nombre"))
                .code((String) row.get("codigo"))
                .name((String) row.get("nombre"))
                .warehouseType((String) row.get("tipo_bodega"))
                .responsibleName((String) row.get("responsable_nombre"))
                .isPrincipal((Boolean) row.get("es_principal"))
                .allowsDispense((Boolean) row.get("permite_dispensar"))
                .allowsReceive((Boolean) row.get("permite_recibir"))
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

package com.cloud_tecnological.mednova.repositories.pagador;

import com.cloud_tecnological.mednova.dto.pagador.PagadorResponseDto;
import com.cloud_tecnological.mednova.dto.pagador.PagadorTableDto;
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
public class PagadorQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PagadorQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<PagadorResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT p.id,
                       p.empresa_id,
                       p.tercero_id,
                       t.nombre_completo,
                       t.numero_documento,
                       td.codigo           AS tipo_documento_codigo,
                       p.codigo,
                       p.tipo_pagador_id,
                       tp.nombre           AS tipo_pagador_nombre,
                       p.tipo_cliente_id,
                       tc.nombre           AS tipo_cliente_nombre,
                       p.codigo_eps,
                       p.codigo_administradora,
                       p.dias_radicacion,
                       p.dias_respuesta_glosa,
                       p.activo,
                       p.created_at
                FROM pagador p
                INNER JOIN tercero t         ON t.id  = p.tercero_id
                INNER JOIN tipo_documento td  ON td.id = t.tipo_documento_id
                INNER JOIN tipo_pagador tp    ON tp.id = p.tipo_pagador_id
                LEFT  JOIN tipo_cliente tc    ON tc.id = p.tipo_cliente_id
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

    public Optional<PagadorResponseDto> findActiveByTercero(Long tercero_id, Long empresa_id) {
        String sql = """
                SELECT p.id,
                       p.empresa_id,
                       p.tercero_id,
                       t.nombre_completo,
                       t.numero_documento,
                       td.codigo           AS tipo_documento_codigo,
                       p.codigo,
                       p.tipo_pagador_id,
                       tp.nombre           AS tipo_pagador_nombre,
                       p.tipo_cliente_id,
                       tc.nombre           AS tipo_cliente_nombre,
                       p.codigo_eps,
                       p.codigo_administradora,
                       p.dias_radicacion,
                       p.dias_respuesta_glosa,
                       p.activo,
                       p.created_at
                FROM pagador p
                INNER JOIN tercero t         ON t.id  = p.tercero_id
                INNER JOIN tipo_documento td  ON td.id = t.tipo_documento_id
                INNER JOIN tipo_pagador tp    ON tp.id = p.tipo_pagador_id
                LEFT  JOIN tipo_cliente tc    ON tc.id = p.tipo_cliente_id
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
                FROM pagador
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

    public boolean existsByCode(String codigo, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*)
                FROM pagador
                WHERE codigo = :codigo
                  AND empresa_id = :empresa_id
                  AND deleted_at IS NULL
                  AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("codigo", codigo)
                .addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<PagadorTableDto> listPagadores(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT p.id,
                       p.tercero_id,
                       t.nombre_completo,
                       td.codigo           AS tipo_documento_codigo,
                       t.numero_documento,
                       p.codigo,
                       tp.nombre           AS tipo_pagador_nombre,
                       p.activo,
                       COUNT(*) OVER()     AS total_rows
                FROM pagador p
                INNER JOIN tercero t         ON t.id  = p.tercero_id
                INNER JOIN tipo_documento td  ON td.id = t.tipo_documento_id
                INNER JOIN tipo_pagador tp    ON tp.id = p.tipo_pagador_id
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
                        OR UPPER(p.codigo) LIKE UPPER(:search)
                    )
                    """);
            params.addValue("search", "%" + search + "%");
            params.addValue("search_exact", search);
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "t.nombre_completo";
        String order = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<PagadorTableDto> dtos = result.stream().map(this::mapToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private PagadorResponseDto mapToResponseDto(Map<String, Object> row) {
        return PagadorResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .fullName((String) row.get("nombre_completo"))
                .documentNumber((String) row.get("numero_documento"))
                .documentTypeCode((String) row.get("tipo_documento_codigo"))
                .code((String) row.get("codigo"))
                .payerTypeId(toLong(row.get("tipo_pagador_id")))
                .payerTypeName((String) row.get("tipo_pagador_nombre"))
                .clientTypeId(toLong(row.get("tipo_cliente_id")))
                .clientTypeName((String) row.get("tipo_cliente_nombre"))
                .epsCode((String) row.get("codigo_eps"))
                .administratorCode((String) row.get("codigo_administradora"))
                .filingDays(toInt(row.get("dias_radicacion")))
                .glossaResponseDays(toInt(row.get("dias_respuesta_glosa")))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private PagadorTableDto mapToTableDto(Map<String, Object> row) {
        return PagadorTableDto.builder()
                .id(toLong(row.get("id")))
                .thirdPartyId(toLong(row.get("tercero_id")))
                .fullName((String) row.get("nombre_completo"))
                .documentTypeCode((String) row.get("tipo_documento_codigo"))
                .documentNumber((String) row.get("numero_documento"))
                .code((String) row.get("codigo"))
                .payerTypeName((String) row.get("tipo_pagador_nombre"))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        return ((Number) v).longValue();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        return ((Number) v).intValue();
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

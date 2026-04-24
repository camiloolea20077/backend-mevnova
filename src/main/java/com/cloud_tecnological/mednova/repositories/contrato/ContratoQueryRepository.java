package com.cloud_tecnological.mednova.repositories.contrato;

import com.cloud_tecnological.mednova.dto.contrato.ContratoResponseDto;
import com.cloud_tecnological.mednova.dto.contrato.ContratoTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ContratoQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ContratoQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ContratoResponseDto> findActiveById(Long id, Long empresa_id) {
        String sql = """
                SELECT c.id, c.empresa_id, c.numero,
                       c.pagador_id,
                       t.nombre_completo  AS pagador_nombre,
                       c.modalidad_pago_id,
                       mp.nombre          AS modalidad_nombre,
                       c.tarifario_id,
                       tar.nombre         AS tarifario_nombre,
                       c.objeto, c.fecha_vigencia_desde, c.fecha_vigencia_hasta,
                       c.valor_contrato, c.techo_mensual, c.observaciones,
                       c.activo, c.created_at
                FROM contrato c
                INNER JOIN pagador p        ON p.id  = c.pagador_id AND p.deleted_at IS NULL
                INNER JOIN tercero t        ON t.id  = p.tercero_id AND t.deleted_at IS NULL
                INNER JOIN modalidad_pago mp ON mp.id = c.modalidad_pago_id
                LEFT  JOIN tarifario tar    ON tar.id = c.tarifario_id AND tar.deleted_at IS NULL
                WHERE c.id = :id
                  AND c.empresa_id = :empresa_id
                  AND c.deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("id", id).addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapToResponseDto(rows.get(0)));
    }

    public boolean existsByNumber(String numero, Long empresa_id, Long excludeId) {
        String sql = """
                SELECT COUNT(*) FROM contrato
                WHERE numero = :numero AND empresa_id = :empresa_id
                  AND deleted_at IS NULL AND id != :exclude_id
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("numero", numero).addValue("empresa_id", empresa_id)
                .addValue("exclude_id", excludeId == null ? -1 : excludeId), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<ContratoTableDto> listContratos(PageableDto<?> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
                SELECT c.id, c.numero,
                       t.nombre_completo  AS pagador_nombre,
                       mp.nombre          AS modalidad_nombre,
                       c.fecha_vigencia_desde, c.fecha_vigencia_hasta,
                       c.activo, COUNT(*) OVER() AS total_rows
                FROM contrato c
                INNER JOIN pagador p        ON p.id  = c.pagador_id AND p.deleted_at IS NULL
                INNER JOIN tercero t        ON t.id  = p.tercero_id AND t.deleted_at IS NULL
                INNER JOIN modalidad_pago mp ON mp.id = c.modalidad_pago_id
                WHERE c.empresa_id = :empresa_id AND c.deleted_at IS NULL
                """);
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("empresa_id", empresa_id);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (UPPER(c.numero) LIKE UPPER(:search) OR unaccent(UPPER(t.nombre_completo)) LIKE unaccent(UPPER(:search)))");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "c.numero";
        String order = "DESC".equalsIgnoreCase(pageable.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows).addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<ContratoTableDto> dtos = result.stream().map(this::mapToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();
        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private ContratoResponseDto mapToResponseDto(Map<String, Object> row) {
        return ContratoResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .number((String) row.get("numero"))
                .payerId(toLong(row.get("pagador_id")))
                .payerName((String) row.get("pagador_nombre"))
                .paymentModalityId(toLong(row.get("modalidad_pago_id")))
                .paymentModalityName((String) row.get("modalidad_nombre"))
                .rateScheduleId(toLong(row.get("tarifario_id")))
                .rateScheduleName((String) row.get("tarifario_nombre"))
                .subject((String) row.get("objeto"))
                .validFrom(toLocalDate(row.get("fecha_vigencia_desde")))
                .validUntil(toLocalDate(row.get("fecha_vigencia_hasta")))
                .contractValue(toBigDecimal(row.get("valor_contrato")))
                .monthlyLimit(toBigDecimal(row.get("techo_mensual")))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private ContratoTableDto mapToTableDto(Map<String, Object> row) {
        return ContratoTableDto.builder()
                .id(toLong(row.get("id")))
                .number((String) row.get("numero"))
                .payerName((String) row.get("pagador_nombre"))
                .paymentModalityName((String) row.get("modalidad_nombre"))
                .validFrom(toLocalDate(row.get("fecha_vigencia_desde")))
                .validUntil(toLocalDate(row.get("fecha_vigencia_hasta")))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private Long toLong(Object v) { return v == null ? null : ((Number) v).longValue(); }

    private BigDecimal toBigDecimal(Object v) { return v == null ? null : new BigDecimal(v.toString()); }

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

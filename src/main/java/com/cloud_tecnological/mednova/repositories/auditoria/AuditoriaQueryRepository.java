package com.cloud_tecnological.mednova.repositories.auditoria;

import com.cloud_tecnological.mednova.dto.auditoria.AuditoriaFilterDto;
import com.cloud_tecnological.mednova.dto.auditoria.AuditoriaResponseDto;
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
public class AuditoriaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AuditoriaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<AuditoriaResponseDto> findById(Long id, Long empresa_id) {
        String sql = """
                SELECT id, empresa_id, sede_id, usuario_id, tabla_afectada, registro_id,
                       accion, datos_antes, datos_despues, ip_origen, user_agent, created_at
                FROM auditoria
                WHERE id = :id
                  AND empresa_id = :empresa_id
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToDto(rows.get(0)));
    }

    public PageImpl<AuditoriaResponseDto> listAuditoria(PageableDto<AuditoriaFilterDto> pageable, Long empresa_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        AuditoriaFilterDto filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT id, empresa_id, sede_id, usuario_id, tabla_afectada, registro_id,
                       accion, datos_antes, datos_despues, ip_origen, user_agent, created_at,
                       COUNT(*) OVER() AS total_rows
                FROM auditoria
                WHERE empresa_id = :empresa_id
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id);

        if (filter != null) {
            if (filter.getTableName() != null && !filter.getTableName().isBlank()) {
                sql.append("AND tabla_afectada = :tabla ");
                params.addValue("tabla", filter.getTableName());
            }
            if (filter.getUserId() != null) {
                sql.append("AND usuario_id = :usuario_id ");
                params.addValue("usuario_id", filter.getUserId());
            }
            if (filter.getAction() != null && !filter.getAction().isBlank()) {
                sql.append("AND accion = :accion ");
                params.addValue("accion", filter.getAction());
            }
            if (filter.getDateFrom() != null) {
                sql.append("AND DATE(created_at) >= :fecha_desde ");
                params.addValue("fecha_desde", filter.getDateFrom());
            }
            if (filter.getDateTo() != null) {
                sql.append("AND DATE(created_at) <= :fecha_hasta ");
                params.addValue("fecha_hasta", filter.getDateTo());
            }
            if (filter.getIpOrigin() != null && !filter.getIpOrigin().isBlank()) {
                sql.append("AND ip_origen = :ip ");
                params.addValue("ip", filter.getIpOrigin());
            }
        }

        sql.append(" ORDER BY created_at DESC OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<AuditoriaResponseDto> dtos = result.stream().map(this::mapRowToDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    private AuditoriaResponseDto mapRowToDto(Map<String, Object> row) {
        return AuditoriaResponseDto.builder()
                .id(toLong(row.get("id")))
                .enterpriseId(toLong(row.get("empresa_id")))
                .branchId(toLong(row.get("sede_id")))
                .userId(toLong(row.get("usuario_id")))
                .tableName((String) row.get("tabla_afectada"))
                .recordId((String) row.get("registro_id"))
                .action((String) row.get("accion"))
                .dataBefore(row.get("datos_antes") != null ? row.get("datos_antes").toString() : null)
                .dataAfter(row.get("datos_despues") != null ? row.get("datos_despues").toString() : null)
                .ipOrigin((String) row.get("ip_origen"))
                .userAgent((String) row.get("user_agent"))
                .createdAt(toLocalDateTime(row.get("created_at")))
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

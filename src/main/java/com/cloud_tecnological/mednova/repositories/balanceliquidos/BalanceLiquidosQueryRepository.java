package com.cloud_tecnological.mednova.repositories.balanceliquidos;

import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceItemResponseDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosFilterParams;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosResponseDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BalanceLiquidosQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public BalanceLiquidosQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── HU-FASE2-088: Lectura por ID con items ──────────────────────────────

    public Optional<BalanceLiquidosResponseDto> findActiveById(Long id, Long empresa_id, Long sede_id) {
        String sqlHeader = """
                SELECT b.id,
                       b.atencion_id,
                       b.paciente_id,
                       b.profesional_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       b.fecha_balance,
                       b.turno,
                       b.total_ingresos,
                       b.total_egresos,
                       b.balance,
                       b.observaciones,
                       b.activo,
                       b.created_at,
                       b.updated_at,
                       b.usuario_creacion,
                       b.usuario_modificacion
                FROM balance_liquidos b
                LEFT JOIN profesional_salud ps ON ps.id = b.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE b.id         = :id
                  AND b.empresa_id = :empresa_id
                  AND b.sede_id    = :sede_id
                  AND b.deleted_at IS NULL
                LIMIT 1
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        List<Map<String, Object>> rows = jdbc.query(sqlHeader, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();

        BalanceLiquidosResponseDto dto = mapRowToResponseDto(rows.get(0));
        dto.setItems(findItemsByBalanceId(id, empresa_id));
        return Optional.of(dto);
    }

    public List<BalanceItemResponseDto> findItemsByBalanceId(Long balance_id, Long empresa_id) {
        String sql = """
                SELECT d.id,
                       d.balance_id,
                       d.tipo,
                       d.via,
                       d.descripcion,
                       d.cantidad_ml,
                       d.hora_registro,
                       d.activo,
                       d.created_at
                FROM detalle_balance_liquidos d
                WHERE d.balance_id = :balance_id
                  AND d.empresa_id = :empresa_id
                  AND d.deleted_at IS NULL
                ORDER BY d.hora_registro ASC, d.id ASC
                """;
        return jdbc.query(sql, new MapSqlParameterSource()
                .addValue("balance_id", balance_id)
                .addValue("empresa_id", empresa_id), new ColumnMapRowMapper())
                .stream().map(this::mapRowToItemDto).toList();
    }

    public PageImpl<BalanceLiquidosTableDto> listBalances(
            PageableDto<BalanceLiquidosFilterParams> pageable, Long empresa_id, Long sede_id) {
        int page = pageable.getPage() != null ? pageable.getPage().intValue() : 0;
        int rows = pageable.getRows() != null ? pageable.getRows().intValue() : 10;
        String search = pageable.getSearch() != null ? pageable.getSearch().trim() : null;
        BalanceLiquidosFilterParams filter = pageable.getParams();

        StringBuilder sql = new StringBuilder("""
                SELECT b.id,
                       b.atencion_id,
                       b.paciente_id,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional_nombre,
                       b.fecha_balance,
                       b.turno,
                       b.total_ingresos,
                       b.total_egresos,
                       b.balance,
                       b.activo,
                       COUNT(*) OVER() AS total_rows
                FROM balance_liquidos b
                LEFT JOIN profesional_salud ps ON ps.id = b.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE b.empresa_id = :empresa_id
                  AND b.sede_id    = :sede_id
                  AND b.deleted_at IS NULL
                """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);

        if (filter != null) {
            if (filter.getEncounterId() != null) {
                sql.append(" AND b.atencion_id = :atencion_id ");
                params.addValue("atencion_id", filter.getEncounterId());
            }
            if (filter.getPatientId() != null) {
                sql.append(" AND b.paciente_id = :paciente_id ");
                params.addValue("paciente_id", filter.getPatientId());
            }
            if (filter.getProfessionalId() != null) {
                sql.append(" AND b.profesional_id = :profesional_id ");
                params.addValue("profesional_id", filter.getProfessionalId());
            }
            if (filter.getShift() != null && !filter.getShift().isBlank()) {
                sql.append(" AND b.turno = :turno ");
                params.addValue("turno", filter.getShift().trim());
            }
            if (filter.getDateFrom() != null) {
                sql.append(" AND b.fecha_balance >= :date_from ");
                params.addValue("date_from", filter.getDateFrom());
            }
            if (filter.getDateTo() != null) {
                sql.append(" AND b.fecha_balance <= :date_to ");
                params.addValue("date_to", filter.getDateTo());
            }
            if (Boolean.TRUE.equals(filter.getOnlyActive())) {
                sql.append(" AND b.activo = true ");
            }
        }

        if (search != null && !search.isEmpty()) {
            sql.append(" AND UPPER(b.observaciones) LIKE UPPER(:search) ");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = pageable.getOrder_by() != null ? pageable.getOrder_by() : "b.fecha_balance";
        String order   = "ASC".equalsIgnoreCase(pageable.getOrder()) ? "ASC" : "DESC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) page * rows);
        params.addValue("limit", rows);

        List<Map<String, Object>> result = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<BalanceLiquidosTableDto> dtos = result.stream().map(this::mapRowToTableDto).toList();
        long total = result.isEmpty() ? 0 : ((Number) result.get(0).get("total_rows")).longValue();

        return new PageImpl<>(dtos, PageRequest.of(page, rows), total);
    }

    // ── Validaciones cross-tenant ───────────────────────────────────────────

    public boolean atencionExistsInTenant(Long atencion_id, Long empresa_id, Long sede_id) {
        String sql = """
                SELECT COUNT(*)
                FROM atencion
                WHERE id         = :id
                  AND empresa_id = :empresa_id
                  AND sede_id    = :sede_id
                  AND deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", atencion_id)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id), Long.class);
        return count != null && count > 0;
    }

    public boolean atencionMatchesPaciente(Long atencion_id, Long paciente_id) {
        String sql = """
                SELECT COUNT(*)
                FROM atencion a
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE a.id          = :atencion_id
                  AND adm.paciente_id = :paciente_id
                  AND a.deleted_at IS NULL
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("atencion_id", atencion_id)
                .addValue("paciente_id", paciente_id), Long.class);
        return count != null && count > 0;
    }

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

    /** Regla: un balance por (atencion, fecha_balance, turno). */
    public boolean existsBalanceForShift(Long atencion_id, LocalDate fecha, String turno,
                                         Long empresa_id, Long sede_id, Long excludeId) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM balance_liquidos
                WHERE atencion_id   = :atencion_id
                  AND fecha_balance = :fecha
                  AND empresa_id    = :empresa_id
                  AND sede_id       = :sede_id
                  AND deleted_at IS NULL
                """);
        if (turno == null) {
            sql.append(" AND turno IS NULL ");
        } else {
            sql.append(" AND turno = :turno ");
        }
        if (excludeId != null) sql.append(" AND id <> :exclude_id ");
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("atencion_id", atencion_id)
                .addValue("fecha", fecha)
                .addValue("empresa_id", empresa_id)
                .addValue("sede_id", sede_id);
        if (turno != null) params.addValue("turno", turno);
        if (excludeId != null) params.addValue("exclude_id", excludeId);

        Long count = jdbc.queryForObject(sql.toString(), params, Long.class);
        return count != null && count > 0;
    }

    /** Sumar montos por tipo (INGRESO/EGRESO) de los detalles activos. */
    public BigDecimal sumDetailsByType(Long balance_id, Long empresa_id, String tipo) {
        String sql = """
                SELECT COALESCE(SUM(cantidad_ml), 0)
                FROM detalle_balance_liquidos
                WHERE balance_id = :balance_id
                  AND empresa_id = :empresa_id
                  AND tipo       = :tipo
                  AND deleted_at IS NULL
                """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("balance_id", balance_id)
                .addValue("empresa_id", empresa_id)
                .addValue("tipo", tipo), BigDecimal.class);
    }

    public Optional<BalanceItemResponseDto> findItemById(Long item_id, Long balance_id, Long empresa_id) {
        String sql = """
                SELECT d.id,
                       d.balance_id,
                       d.tipo,
                       d.via,
                       d.descripcion,
                       d.cantidad_ml,
                       d.hora_registro,
                       d.activo,
                       d.created_at
                FROM detalle_balance_liquidos d
                WHERE d.id         = :id
                  AND d.balance_id = :balance_id
                  AND d.empresa_id = :empresa_id
                  AND d.deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("id", item_id)
                .addValue("balance_id", balance_id)
                .addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRowToItemDto(rows.get(0)));
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private BalanceLiquidosResponseDto mapRowToResponseDto(Map<String, Object> row) {
        return BalanceLiquidosResponseDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .professionalId(toLong(row.get("profesional_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .balanceDate(toLocalDate(row.get("fecha_balance")))
                .shift((String) row.get("turno"))
                .totalIngresos(toBigDecimal(row.get("total_ingresos")))
                .totalEgresos(toBigDecimal(row.get("total_egresos")))
                .balance(toBigDecimal(row.get("balance")))
                .observations((String) row.get("observaciones"))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .updatedAt(toLocalDateTime(row.get("updated_at")))
                .createdById(toLong(row.get("usuario_creacion")))
                .updatedById(toLong(row.get("usuario_modificacion")))
                .build();
    }

    private BalanceLiquidosTableDto mapRowToTableDto(Map<String, Object> row) {
        return BalanceLiquidosTableDto.builder()
                .id(toLong(row.get("id")))
                .encounterId(toLong(row.get("atencion_id")))
                .patientId(toLong(row.get("paciente_id")))
                .professionalName((String) row.get("profesional_nombre"))
                .balanceDate(toLocalDate(row.get("fecha_balance")))
                .shift((String) row.get("turno"))
                .totalIngresos(toBigDecimal(row.get("total_ingresos")))
                .totalEgresos(toBigDecimal(row.get("total_egresos")))
                .balance(toBigDecimal(row.get("balance")))
                .active((Boolean) row.get("activo"))
                .build();
    }

    private BalanceItemResponseDto mapRowToItemDto(Map<String, Object> row) {
        return BalanceItemResponseDto.builder()
                .id(toLong(row.get("id")))
                .balanceId(toLong(row.get("balance_id")))
                .type((String) row.get("tipo"))
                .route((String) row.get("via"))
                .description((String) row.get("descripcion"))
                .amountMl(toBigDecimal(row.get("cantidad_ml")))
                .recordedAt(toLocalTime(row.get("hora_registro")))
                .active((Boolean) row.get("activo"))
                .createdAt(toLocalDateTime(row.get("created_at")))
                .build();
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return null;
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return null;
    }

    private LocalTime toLocalTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalTime lt) return lt;
        if (value instanceof Time t) return t.toLocalTime();
        return null;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

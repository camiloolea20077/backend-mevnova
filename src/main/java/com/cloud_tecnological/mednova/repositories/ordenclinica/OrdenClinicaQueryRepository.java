package com.cloud_tecnological.mednova.repositories.ordenclinica;

import com.cloud_tecnological.mednova.dto.ordenclinica.OrdenClinicaResponseDto;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OrdenClinicaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OrdenClinicaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoOrdenIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_orden WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public boolean existsTipoOrdenClinica(String codigo) {
        String sql = "SELECT COUNT(*) FROM tipo_orden_clinica WHERE codigo = :codigo AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("codigo", codigo), Long.class);
        return count != null && count > 0;
    }

    public boolean existsServicioSalud(Long servicioId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM servicio_salud
            WHERE id = :id AND empresa_id = :empresa_id AND activo = true AND deleted_at IS NULL
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", servicioId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public String generateNumeroOrden(Long empresaId) {
        String sql = """
            SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(numero_orden, '[^0-9]', '', 'g') AS BIGINT)), 0) + 1
            FROM orden_clinica
            WHERE empresa_id = :empresa_id
        """;
        Long next = jdbc.queryForObject(sql, new MapSqlParameterSource("empresa_id", empresaId), Long.class);
        return String.format("ORD-%06d", next);
    }

    public Optional<Long> findProfesionalByUsuario(Long usuarioId, Long empresaId) {
        String sql = """
            SELECT ps.id
            FROM profesional_salud ps
            INNER JOIN usuario u ON u.tercero_id = ps.tercero_id
            WHERE u.id = :usuario_id
              AND ps.empresa_id = :empresa_id
              AND ps.activo = true
              AND ps.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("usuario_id", usuarioId)
            .addValue("empresa_id", empresaId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(((Number) rows.get(0).get("id")).longValue());
    }

    public List<OrdenClinicaResponseDto> findByAtencionId(Long atencionId, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                oc.id,
                oc.atencion_id,
                oc.empresa_id,
                oc.sede_id,
                oc.numero_orden,
                oc.tipo_orden,
                toc.nombre               AS tipo_orden_nombre,
                oc.estado_orden,
                eo.nombre                AS estado_orden_nombre,
                oc.profesional_id,
                t.nombre_completo        AS profesional_nombre,
                oc.fecha_orden,
                oc.observaciones,
                oc.activo,
                oc.created_at,
                doc.id                   AS detalle_id,
                doc.servicio_salud_id    AS detalle_servicio_id,
                ss.nombre                AS detalle_servicio_nombre,
                ss.codigo_interno        AS detalle_codigo_interno,
                doc.cantidad             AS detalle_cantidad,
                doc.indicaciones         AS detalle_indicaciones,
                doc.urgencia             AS detalle_urgencia
            FROM orden_clinica oc
            INNER JOIN tipo_orden_clinica toc ON toc.codigo = oc.tipo_orden
            INNER JOIN estado_orden eo        ON eo.codigo  = oc.estado_orden
            LEFT  JOIN profesional_salud ps   ON ps.id  = oc.profesional_id
            LEFT  JOIN tercero t              ON t.id   = ps.tercero_id AND t.deleted_at IS NULL
            LEFT  JOIN detalle_orden_clinica doc ON doc.orden_clinica_id = oc.id AND doc.activo = true
            LEFT  JOIN servicio_salud ss      ON ss.id  = doc.servicio_salud_id
            WHERE oc.atencion_id  = :atencion_id
              AND oc.empresa_id   = :empresa_id
              AND oc.sede_id      = :sede_id
              AND oc.deleted_at  IS NULL
            ORDER BY oc.id, doc.id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("atencion_id", atencionId)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return assembleOrdenesFromRows(rows);
    }

    private List<OrdenClinicaResponseDto> assembleOrdenesFromRows(List<Map<String, Object>> rows) {
        Map<Long, OrdenClinicaResponseDto> ordenes = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long ordenId = toLong(row.get("id"));
            OrdenClinicaResponseDto orden = ordenes.computeIfAbsent(ordenId, k -> {
                OrdenClinicaResponseDto o = new OrdenClinicaResponseDto();
                o.setId(toLong(row.get("id")));
                o.setAtencionId(toLong(row.get("atencion_id")));
                o.setEmpresaId(toLong(row.get("empresa_id")));
                o.setSedeId(toLong(row.get("sede_id")));
                o.setNumeroOrden((String) row.get("numero_orden"));
                o.setTipoOrden((String) row.get("tipo_orden"));
                o.setTipoOrdenNombre((String) row.get("tipo_orden_nombre"));
                o.setEstadoOrden((String) row.get("estado_orden"));
                o.setEstadoOrdenNombre((String) row.get("estado_orden_nombre"));
                o.setProfesionalId(toLong(row.get("profesional_id")));
                o.setProfesionalNombre((String) row.get("profesional_nombre"));
                o.setFechaOrden(toLocalDateTime(row.get("fecha_orden")));
                o.setObservaciones((String) row.get("observaciones"));
                o.setActivo((Boolean) row.get("activo"));
                o.setCreatedAt(toLocalDateTime(row.get("created_at")));
                o.setItems(new ArrayList<>());
                return o;
            });

            if (row.get("detalle_id") != null) {
                OrdenClinicaResponseDto.DetalleOrdenResponseDto item = new OrdenClinicaResponseDto.DetalleOrdenResponseDto();
                item.setId(toLong(row.get("detalle_id")));
                item.setServicioSaludId(toLong(row.get("detalle_servicio_id")));
                item.setServicioNombre((String) row.get("detalle_servicio_nombre"));
                item.setCodigoInterno((String) row.get("detalle_codigo_interno"));
                if (row.get("detalle_cantidad") != null) {
                    item.setCantidad(new BigDecimal(row.get("detalle_cantidad").toString()));
                }
                item.setIndicaciones((String) row.get("detalle_indicaciones"));
                item.setUrgencia((String) row.get("detalle_urgencia"));
                orden.getItems().add(item);
            }
        }
        return new ArrayList<>(ordenes.values());
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Timestamp) return ((Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }
}

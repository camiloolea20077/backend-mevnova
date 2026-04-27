package com.cloud_tecnological.mednova.repositories.prescripcion;

import com.cloud_tecnological.mednova.dto.prescripcion.PrescripcionResponseDto;
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
public class PrescripcionQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PrescripcionQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoPrescripcionIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_prescripcion WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public String generateNumeroPrescripcion(Long empresaId) {
        String sql = """
            SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(numero_prescripcion, '[^0-9]', '', 'g') AS BIGINT)), 0) + 1
            FROM prescripcion
            WHERE empresa_id = :empresa_id
        """;
        Long next = jdbc.queryForObject(sql, new MapSqlParameterSource("empresa_id", empresaId), Long.class);
        return String.format("PRE-%06d", next);
    }

    public boolean existsViaAdministracion(Long id) {
        String sql = "SELECT COUNT(*) FROM via_administracion WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), Long.class);
        return count != null && count > 0;
    }

    public boolean existsFrecuenciaDosis(Long id) {
        String sql = "SELECT COUNT(*) FROM frecuencia_dosis WHERE id = :id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("id", id), Long.class);
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

    public List<PrescripcionResponseDto> findByAtencionId(Long atencionId, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                pr.id,
                pr.atencion_id,
                pr.empresa_id,
                pr.sede_id,
                pr.numero_prescripcion,
                pr.estado_prescripcion_id,
                ep.nombre               AS estado_prescripcion_nombre,
                pr.profesional_id,
                t.nombre_completo       AS profesional_nombre,
                pr.fecha_prescripcion,
                pr.observaciones,
                pr.activo,
                pr.created_at,
                dp.id                   AS detalle_id,
                dp.servicio_salud_id    AS detalle_servicio_id,
                ss.nombre               AS detalle_med_nombre,
                dp.dosis                AS detalle_dosis,
                dp.unidad_dosis         AS detalle_unidad_dosis,
                dp.via_administracion_id AS detalle_via_id,
                va.nombre               AS detalle_via_nombre,
                dp.frecuencia_dosis_id  AS detalle_frecuencia_id,
                fd.nombre               AS detalle_frecuencia_nombre,
                dp.duracion_dias        AS detalle_duracion_dias,
                dp.cantidad_despachar   AS detalle_cantidad_despachar,
                dp.indicaciones         AS detalle_indicaciones
            FROM prescripcion pr
            INNER JOIN estado_prescripcion ep ON ep.id = pr.estado_prescripcion_id
            LEFT  JOIN profesional_salud ps   ON ps.id = pr.profesional_id
            LEFT  JOIN tercero t              ON t.id  = ps.tercero_id AND t.deleted_at IS NULL
            LEFT  JOIN detalle_prescripcion dp ON dp.prescripcion_id = pr.id AND dp.activo = true
            LEFT  JOIN servicio_salud ss      ON ss.id = dp.servicio_salud_id
            LEFT  JOIN via_administracion va  ON va.id = dp.via_administracion_id
            LEFT  JOIN frecuencia_dosis fd    ON fd.id = dp.frecuencia_dosis_id
            WHERE pr.atencion_id = :atencion_id
              AND pr.empresa_id  = :empresa_id
              AND pr.sede_id     = :sede_id
              AND pr.deleted_at IS NULL
            ORDER BY pr.id, dp.id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("atencion_id", atencionId)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return assemblePrescripcionesFromRows(rows);
    }

    private List<PrescripcionResponseDto> assemblePrescripcionesFromRows(List<Map<String, Object>> rows) {
        Map<Long, PrescripcionResponseDto> prescripciones = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Long prescId = toLong(row.get("id"));
            PrescripcionResponseDto presc = prescripciones.computeIfAbsent(prescId, k -> {
                PrescripcionResponseDto p = new PrescripcionResponseDto();
                p.setId(toLong(row.get("id")));
                p.setAtencionId(toLong(row.get("atencion_id")));
                p.setEmpresaId(toLong(row.get("empresa_id")));
                p.setSedeId(toLong(row.get("sede_id")));
                p.setNumeroPrescripcion((String) row.get("numero_prescripcion"));
                p.setEstadoPrescripcionId(toLong(row.get("estado_prescripcion_id")));
                p.setEstadoPrescripcionNombre((String) row.get("estado_prescripcion_nombre"));
                p.setProfesionalId(toLong(row.get("profesional_id")));
                p.setProfesionalNombre((String) row.get("profesional_nombre"));
                p.setFechaPrescripcion(toLocalDateTime(row.get("fecha_prescripcion")));
                p.setObservaciones((String) row.get("observaciones"));
                p.setActivo((Boolean) row.get("activo"));
                p.setCreatedAt(toLocalDateTime(row.get("created_at")));
                p.setItems(new ArrayList<>());
                return p;
            });

            if (row.get("detalle_id") != null) {
                PrescripcionResponseDto.DetallePrescripcionResponseDto item = new PrescripcionResponseDto.DetallePrescripcionResponseDto();
                item.setId(toLong(row.get("detalle_id")));
                item.setServicioSaludId(toLong(row.get("detalle_servicio_id")));
                item.setMedicamentoNombre((String) row.get("detalle_med_nombre"));
                if (row.get("detalle_dosis") != null) {
                    item.setDosis(new BigDecimal(row.get("detalle_dosis").toString()));
                }
                item.setUnidadDosis((String) row.get("detalle_unidad_dosis"));
                item.setViaAdministracionId(toLong(row.get("detalle_via_id")));
                item.setViaNombre((String) row.get("detalle_via_nombre"));
                item.setFrecuenciaDosisId(toLong(row.get("detalle_frecuencia_id")));
                item.setFrecuenciaNombre((String) row.get("detalle_frecuencia_nombre"));
                if (row.get("detalle_duracion_dias") != null) {
                    item.setDuracionDias(((Number) row.get("detalle_duracion_dias")).intValue());
                }
                if (row.get("detalle_cantidad_despachar") != null) {
                    item.setCantidadDespachar(new BigDecimal(row.get("detalle_cantidad_despachar").toString()));
                }
                item.setIndicaciones((String) row.get("detalle_indicaciones"));
                presc.getItems().add(item);
            }
        }
        return new ArrayList<>(prescripciones.values());
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

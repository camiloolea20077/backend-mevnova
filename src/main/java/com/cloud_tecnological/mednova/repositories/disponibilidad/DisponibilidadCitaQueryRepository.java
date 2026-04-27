package com.cloud_tecnological.mednova.repositories.disponibilidad;

import com.cloud_tecnological.mednova.dto.disponibilidad.DisponibilidadResponseDto;
import com.cloud_tecnological.mednova.dto.disponibilidad.SearchDisponibilidadRequestDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DisponibilidadCitaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DisponibilidadCitaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoDisponibilidadIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_disponibilidad WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public Long insertIfNotExists(Long empresaId, Long sedeId, Long agendaId,
                                   LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                   Integer cuposTotales, Long estadoDisponibilidadId) {
        String sql = """
            INSERT INTO disponibilidad_cita
                (empresa_id, sede_id, agenda_id, fecha, hora_inicio, hora_fin,
                 cupos_totales, cupos_ocupados, estado_disponibilidad_id, created_at)
            VALUES
                (:empresa_id, :sede_id, :agenda_id, :fecha, :hora_inicio, :hora_fin,
                 :cupos_totales, 0, :estado_disponibilidad_id, NOW())
            ON CONFLICT (agenda_id, fecha, hora_inicio) DO NOTHING
            RETURNING id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId)
            .addValue("agenda_id", agendaId)
            .addValue("fecha", fecha)
            .addValue("hora_inicio", horaInicio)
            .addValue("hora_fin", horaFin)
            .addValue("cupos_totales", cuposTotales)
            .addValue("estado_disponibilidad_id", estadoDisponibilidadId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (!rows.isEmpty()) return ((Number) rows.get(0).get("id")).longValue();

        String selectSql = """
            SELECT id FROM disponibilidad_cita
            WHERE agenda_id = :agenda_id AND fecha = :fecha AND hora_inicio = :hora_inicio
            LIMIT 1
        """;
        List<Map<String, Object>> existing = jdbc.query(selectSql, new MapSqlParameterSource()
            .addValue("agenda_id", agendaId)
            .addValue("fecha", fecha)
            .addValue("hora_inicio", horaInicio), new ColumnMapRowMapper());
        return existing.isEmpty() ? null : ((Number) existing.get(0).get("id")).longValue();
    }

    public List<DisponibilidadResponseDto> searchAvailable(SearchDisponibilidadRequestDto req,
                                                            Long empresaId, Long sedeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                d.id,
                d.agenda_id                                 AS "agendaId",
                ap.profesional_id                           AS "profesionalId",
                tp.nombre_completo                          AS "profesionalNombre",
                ap.especialidad_id                          AS "especialidadId",
                esp.nombre                                  AS "especialidadNombre",
                rf.nombre                                   AS "recursoFisicoNombre",
                d.fecha,
                d.hora_inicio                               AS "horaInicio",
                d.hora_fin                                  AS "horaFin",
                d.cupos_totales                             AS "cuposTotales",
                d.cupos_ocupados                            AS "cuposOcupados",
                (d.cupos_totales - d.cupos_ocupados)        AS "cuposDisponibles",
                ed.codigo                                   AS "estadoDisponibilidad",
                ed.nombre                                   AS "estadoDisponibilidadNombre"
            FROM disponibilidad_cita d
            INNER JOIN agenda_profesional ap ON ap.id = d.agenda_id AND ap.activo = true
            INNER JOIN profesional_salud ps ON ps.id = ap.profesional_id
            INNER JOIN tercero tp ON tp.id = ps.tercero_id
            LEFT JOIN especialidad esp ON esp.id = ap.especialidad_id
            LEFT JOIN recurso_fisico rf ON rf.id = ap.recurso_fisico_id
            INNER JOIN estado_disponibilidad ed ON ed.id = d.estado_disponibilidad_id
            WHERE d.empresa_id = :empresa_id AND d.sede_id = :sede_id
              AND ed.codigo = 'DISPONIBLE'
              AND d.cupos_ocupados < d.cupos_totales
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        if (req.getAgendaId() != null) {
            sql.append(" AND d.agenda_id = :agenda_id");
            params.addValue("agenda_id", req.getAgendaId());
        }
        if (req.getProfesionalId() != null) {
            sql.append(" AND ap.profesional_id = :profesional_id");
            params.addValue("profesional_id", req.getProfesionalId());
        }
        if (req.getEspecialidadId() != null) {
            sql.append(" AND ap.especialidad_id = :especialidad_id");
            params.addValue("especialidad_id", req.getEspecialidadId());
        }
        if (req.getFechaDesde() != null) {
            sql.append(" AND d.fecha >= :fecha_desde");
            params.addValue("fecha_desde", req.getFechaDesde());
        }
        if (req.getFechaHasta() != null) {
            sql.append(" AND d.fecha <= :fecha_hasta");
            params.addValue("fecha_hasta", req.getFechaHasta());
        }

        sql.append(" ORDER BY d.fecha, d.hora_inicio LIMIT 200");
        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        return MapperRepository.mapListToDtoList(rows, DisponibilidadResponseDto.class);
    }

    public boolean decrementCupos(Long disponibilidadId, Long estadoOcupadoId) {
        String sql = """
            UPDATE disponibilidad_cita
            SET cupos_ocupados = cupos_ocupados + 1,
                estado_disponibilidad_id = CASE
                    WHEN cupos_ocupados + 1 >= cupos_totales THEN :estado_ocupado_id
                    ELSE estado_disponibilidad_id
                END
            WHERE id = :id AND cupos_ocupados < cupos_totales
        """;
        int updated = jdbc.update(sql, new MapSqlParameterSource()
            .addValue("id", disponibilidadId)
            .addValue("estado_ocupado_id", estadoOcupadoId));
        return updated > 0;
    }

    public void incrementCupos(Long disponibilidadId, Long estadoDisponibleId) {
        String sql = """
            UPDATE disponibilidad_cita
            SET cupos_ocupados = GREATEST(cupos_ocupados - 1, 0),
                estado_disponibilidad_id = :estado_disponible_id
            WHERE id = :id
        """;
        jdbc.update(sql, new MapSqlParameterSource()
            .addValue("id", disponibilidadId)
            .addValue("estado_disponible_id", estadoDisponibleId));
    }

    public Optional<DisponibilidadResponseDto> findById(Long id, Long empresaId) {
        String sql = """
            SELECT
                d.id,
                d.agenda_id                                 AS "agendaId",
                ap.profesional_id                           AS "profesionalId",
                tp.nombre_completo                          AS "profesionalNombre",
                ap.especialidad_id                          AS "especialidadId",
                esp.nombre                                  AS "especialidadNombre",
                rf.nombre                                   AS "recursoFisicoNombre",
                d.fecha,
                d.hora_inicio                               AS "horaInicio",
                d.hora_fin                                  AS "horaFin",
                d.cupos_totales                             AS "cuposTotales",
                d.cupos_ocupados                            AS "cuposOcupados",
                (d.cupos_totales - d.cupos_ocupados)        AS "cuposDisponibles",
                ed.codigo                                   AS "estadoDisponibilidad",
                ed.nombre                                   AS "estadoDisponibilidadNombre"
            FROM disponibilidad_cita d
            INNER JOIN agenda_profesional ap ON ap.id = d.agenda_id
            INNER JOIN profesional_salud ps ON ps.id = ap.profesional_id
            INNER JOIN tercero tp ON tp.id = ps.tercero_id
            LEFT JOIN especialidad esp ON esp.id = ap.especialidad_id
            LEFT JOIN recurso_fisico rf ON rf.id = ap.recurso_fisico_id
            INNER JOIN estado_disponibilidad ed ON ed.id = d.estado_disponibilidad_id
            WHERE d.id = :id AND d.empresa_id = :empresa_id
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(MapperRepository.mapListToDtoList(rows, DisponibilidadResponseDto.class).get(0));
    }
}

package com.cloud_tecnological.mednova.repositories.agenda;

import com.cloud_tecnological.mednova.dto.agenda.AgendaTableDto;
import com.cloud_tecnological.mednova.dto.agenda.AgendaResponseDto;
import com.cloud_tecnological.mednova.dto.agenda.BloqueAgendaResponseDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AgendaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AgendaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoAgendaIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_agenda WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public boolean existsProfesionalInEmpresa(Long profesionalId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM profesional_salud
            WHERE id = :id AND empresa_id = :empresa_id AND activo = true AND deleted_at IS NULL
        """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
            .addValue("id", profesionalId)
            .addValue("empresa_id", empresaId), Long.class);
        return count != null && count > 0;
    }

    public boolean existsCalendarioInEmpresa(Long calendarioId, Long empresaId) {
        String sql = "SELECT COUNT(*) FROM calendario_cita WHERE id = :id AND empresa_id = :empresa_id AND activo = true";
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
            .addValue("id", calendarioId)
            .addValue("empresa_id", empresaId), Long.class);
        return count != null && count > 0;
    }

    public Optional<AgendaResponseDto> findActiveById(Long id, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                a.id,
                a.profesional_id        AS "profesionalId",
                tp.nombre_completo      AS "profesionalNombre",
                a.especialidad_id       AS "especialidadId",
                ss.nombre               AS "especialidadNombre",
                a.recurso_fisico_id     AS "recursoFisicoId",
                rf.nombre               AS "recursoFisicoNombre",
                a.calendario_id         AS "calendarioId",
                cal.nombre              AS "calendarioNombre",
                a.estado_agenda_id      AS "estadoAgendaId",
                ea.nombre               AS "estadoAgendaNombre",
                ea.codigo               AS "estadoAgendaCodigo",
                a.duracion_cita_minutos AS "duracionCitaMinutos",
                a.fecha_vigencia_desde  AS "fechaVigenciaDesde",
                a.fecha_vigencia_hasta  AS "fechaVigenciaHasta",
                a.observaciones,
                a.activo,
                a.created_at
            FROM agenda_profesional a
            INNER JOIN profesional_salud ps ON ps.id = a.profesional_id
            INNER JOIN tercero tp ON tp.id = ps.tercero_id
            LEFT JOIN especialidad ss ON ss.id = a.especialidad_id
            LEFT JOIN recurso_fisico rf ON rf.id = a.recurso_fisico_id
            INNER JOIN calendario_cita cal ON cal.id = a.calendario_id
            INNER JOIN estado_agenda ea ON ea.id = a.estado_agenda_id
            WHERE a.id = :id AND a.empresa_id = :empresa_id AND a.sede_id = :sede_id AND a.activo = true
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<AgendaResponseDto> mapped = MapperRepository.mapListToDtoList(rows, AgendaResponseDto.class);
        AgendaResponseDto dto = mapped.get(0);

        String bloqueSql = """
            SELECT id,
                   dia_semana  AS "diaSemana",
                   hora_inicio AS "horaInicio",
                   hora_fin    AS "horaFin",
                   cupos
            FROM bloque_agenda
            WHERE agenda_id = :agenda_id
            ORDER BY dia_semana, hora_inicio
        """;
        List<Map<String, Object>> bloqueRows = jdbc.query(bloqueSql,
            new MapSqlParameterSource("agenda_id", id), new ColumnMapRowMapper());
        List<BloqueAgendaResponseDto> bloques = MapperRepository.mapListToDtoList(bloqueRows, BloqueAgendaResponseDto.class);

        String[] labels = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        for (BloqueAgendaResponseDto b : bloques) {
            if (b.getDiaSemana() != null && b.getDiaSemana() >= 1 && b.getDiaSemana() <= 7) {
                b.setDiaSemanaLabel(labels[b.getDiaSemana()]);
            }
        }
        dto.setBloques(bloques);
        return Optional.of(dto);
    }

    public PageImpl<AgendaTableDto> listActivos(PageableDto<?> request, Long empresaId, Long sedeId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize   = request.getRows() != null ? request.getRows().intValue() : 10;
        String search  = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT
                a.id,
                tp.nombre_completo      AS "profesionalNombre",
                ss.nombre               AS "especialidadNombre",
                cal.nombre              AS "calendarioNombre",
                ea.nombre               AS "estadoAgendaNombre",
                a.duracion_cita_minutos AS "duracionCitaMinutos",
                a.fecha_vigencia_desde  AS "fechaVigenciaDesde",
                a.fecha_vigencia_hasta  AS "fechaVigenciaHasta",
                a.activo,
                COUNT(*) OVER()         AS total_rows
            FROM agenda_profesional a
            INNER JOIN profesional_salud ps ON ps.id = a.profesional_id
            INNER JOIN tercero tp ON tp.id = ps.tercero_id
            LEFT JOIN especialidad ss ON ss.id = a.especialidad_id
            INNER JOIN calendario_cita cal ON cal.id = a.calendario_id
            INNER JOIN estado_agenda ea ON ea.id = a.estado_agenda_id
            WHERE a.empresa_id = :empresa_id AND a.sede_id = :sede_id AND a.activo = true
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (tp.nombre_completo ILIKE :search OR ss.nombre ILIKE :search)");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = request.getOrder_by() != null ? request.getOrder_by() : "a.created_at";
        String order   = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<AgendaTableDto> result = MapperRepository.mapListToDtoList(rows, AgendaTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();
        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }
}

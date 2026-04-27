package com.cloud_tecnological.mednova.repositories.cita;

import com.cloud_tecnological.mednova.dto.cita.AppointmentResponseDto;
import com.cloud_tecnological.mednova.dto.cita.AppointmentTableDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CitaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CitaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoCitaIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_cita WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public String generateNumeroCita(Long empresaId, Long sedeId) {
        String sql = """
            SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(numero_cita, '[^0-9]', '', 'g') AS BIGINT)), 0) + 1
            FROM cita WHERE empresa_id = :empresa_id AND sede_id = :sede_id
        """;
        Long next = jdbc.queryForObject(sql,
            new MapSqlParameterSource().addValue("empresa_id", empresaId).addValue("sede_id", sedeId),
            Long.class);
        return String.format("CIT-%06d", next);
    }

    public Optional<AppointmentResponseDto> findActiveById(Long id, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                c.id,
                c.numero_cita           AS "numeroCita",
                c.disponibilidad_id     AS "disponibilidadId",
                c.agenda_id             AS "agendaId",
                c.paciente_id           AS "pacienteId",
                tpac.nombre_completo    AS "pacienteNombre",
                tpac.numero_documento   AS "documentoNumero",
                ap.profesional_id       AS "profesionalId",
                tpro.nombre_completo    AS "profesionalNombre",
                c.servicio_salud_id     AS "servicioSaludId",
                ss.nombre               AS "servicioSaludNombre",
                c.tipo_cita_id          AS "tipoCitaId",
                tc.nombre               AS "tipoCitaNombre",
                c.estado_cita_id        AS "estadoCitaId",
                ec.codigo               AS "estadoCitaCodigo",
                ec.nombre               AS "estadoCitaNombre",
                c.especialidad_id       AS "especialidadId",
                esp.nombre              AS "especialidadNombre",
                d.fecha                 AS "fechaCita",
                d.hora_inicio           AS "horaCita",
                c.motivo,
                c.observaciones,
                c.created_at
            FROM cita c
            INNER JOIN disponibilidad_cita d ON d.id = c.disponibilidad_id
            INNER JOIN agenda_profesional ap ON ap.id = c.agenda_id
            INNER JOIN profesional_salud ps ON ps.id = ap.profesional_id
            INNER JOIN tercero tpro ON tpro.id = ps.tercero_id
            INNER JOIN paciente pac ON pac.id = c.paciente_id
            INNER JOIN tercero tpac ON tpac.id = pac.tercero_id
            LEFT JOIN servicio_salud ss ON ss.id = c.servicio_salud_id
            INNER JOIN tipo_cita tc ON tc.id = c.tipo_cita_id
            INNER JOIN estado_cita ec ON ec.id = c.estado_cita_id
            LEFT JOIN especialidad esp ON esp.id = c.especialidad_id
            WHERE c.id = :id AND c.empresa_id = :empresa_id AND c.sede_id = :sede_id AND c.activo = true
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(MapperRepository.mapListToDtoList(rows, AppointmentResponseDto.class).get(0));
    }

    public PageImpl<AppointmentTableDto> listActive(PageableDto<?> request, Long empresaId, Long sedeId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize   = request.getRows() != null ? request.getRows().intValue() : 10;
        String search  = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT
                c.id,
                c.numero_cita           AS "numeroCita",
                tpac.nombre_completo    AS "pacienteNombre",
                tpac.numero_documento   AS "documentoNumero",
                tpro.nombre_completo    AS "profesionalNombre",
                esp.nombre              AS "especialidadNombre",
                d.fecha                 AS "fechaCita",
                d.hora_inicio           AS "horaCita",
                ec.nombre               AS "estadoCitaNombre",
                COUNT(*) OVER()         AS total_rows
            FROM cita c
            INNER JOIN disponibilidad_cita d ON d.id = c.disponibilidad_id
            INNER JOIN agenda_profesional ap ON ap.id = c.agenda_id
            INNER JOIN profesional_salud ps ON ps.id = ap.profesional_id
            INNER JOIN tercero tpro ON tpro.id = ps.tercero_id
            INNER JOIN paciente pac ON pac.id = c.paciente_id
            INNER JOIN tercero tpac ON tpac.id = pac.tercero_id
            LEFT JOIN especialidad esp ON esp.id = c.especialidad_id
            INNER JOIN estado_cita ec ON ec.id = c.estado_cita_id
            INNER JOIN tipo_cita tc ON tc.id = c.tipo_cita_id
            WHERE c.empresa_id = :empresa_id AND c.sede_id = :sede_id AND c.activo = true
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (tpac.nombre_completo ILIKE :search OR tpac.numero_documento ILIKE :search OR c.numero_cita ILIKE :search)");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = request.getOrder_by() != null ? request.getOrder_by() : "d.fecha";
        String order   = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<AppointmentTableDto> result = MapperRepository.mapListToDtoList(rows, AppointmentTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();
        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }

    public List<Map<String, Object>> findAgendadasByAgendaAndFecha(Long agendaId, LocalDate fecha) {
        String sql = """
            SELECT c.id, c.disponibilidad_id, c.paciente_id, c.tipo_cita_id,
                   c.servicio_salud_id, c.especialidad_id, c.motivo, c.observaciones
            FROM cita c
            INNER JOIN estado_cita ec ON ec.id = c.estado_cita_id
            WHERE c.agenda_id = :agenda_id AND ec.codigo = 'AGENDADA' AND c.activo = true
              AND EXISTS (
                  SELECT 1 FROM disponibilidad_cita d
                  WHERE d.id = c.disponibilidad_id AND d.fecha = :fecha
              )
        """;
        return jdbc.query(sql, new MapSqlParameterSource()
            .addValue("agenda_id", agendaId)
            .addValue("fecha", fecha), new ColumnMapRowMapper());
    }

    public boolean existsPacienteInEmpresa(Long pacienteId, Long empresaId) {
        String sql = "SELECT COUNT(*) FROM paciente WHERE id = :id AND empresa_id = :empresa_id AND deleted_at IS NULL";
        Long count = jdbc.queryForObject(sql,
            new MapSqlParameterSource().addValue("id", pacienteId).addValue("empresa_id", empresaId),
            Long.class);
        return count != null && count > 0;
    }

    public boolean hasCitaActivaEnSlot(Long pacienteId, Long disponibilidadId) {
        String sql = """
            SELECT COUNT(*) FROM cita c
            INNER JOIN estado_cita ec ON ec.id = c.estado_cita_id
            WHERE c.paciente_id = :paciente_id AND c.disponibilidad_id = :disponibilidad_id
              AND ec.codigo = 'AGENDADA' AND c.activo = true
        """;
        Long count = jdbc.queryForObject(sql,
            new MapSqlParameterSource().addValue("paciente_id", pacienteId).addValue("disponibilidad_id", disponibilidadId),
            Long.class);
        return count != null && count > 0;
    }
}

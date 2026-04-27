package com.cloud_tecnological.mednova.repositories.listaespera;

import com.cloud_tecnological.mednova.dto.listaespera.WaitListResponseDto;
import com.cloud_tecnological.mednova.dto.listaespera.WaitListTableDto;
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
public class ListaEsperaCitaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ListaEsperaCitaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<WaitListResponseDto> findActiveById(Long id, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                le.id,
                le.paciente_id              AS "pacienteId",
                tp.nombre_completo          AS "pacienteNombre",
                tp.numero_documento         AS "documentoNumero",
                le.especialidad_id          AS "especialidadId",
                esp.nombre                  AS "especialidadNombre",
                le.servicio_salud_id        AS "servicioSaludId",
                ss.nombre                   AS "servicioSaludNombre",
                le.prioridad,
                le.fecha_preferida_desde    AS "fechaPreferidaDesde",
                le.fecha_preferida_hasta    AS "fechaPreferidaHasta",
                le.estado,
                le.observaciones,
                le.created_at
            FROM lista_espera_cita le
            INNER JOIN paciente pac ON pac.id = le.paciente_id
            INNER JOIN tercero tp ON tp.id = pac.tercero_id
            LEFT JOIN especialidad esp ON esp.id = le.especialidad_id
            LEFT JOIN servicio_salud ss ON ss.id = le.servicio_salud_id
            WHERE le.id = :id AND le.empresa_id = :empresa_id AND le.sede_id = :sede_id AND le.activo = true
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(MapperRepository.mapListToDtoList(rows, WaitListResponseDto.class).get(0));
    }

    public PageImpl<WaitListTableDto> listActivos(PageableDto<?> request, Long empresaId, Long sedeId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize   = request.getRows() != null ? request.getRows().intValue() : 10;
        String search  = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT
                le.id,
                tp.nombre_completo          AS "pacienteNombre",
                tp.numero_documento         AS "documentoNumero",
                esp.nombre                  AS "especialidadNombre",
                le.prioridad,
                le.estado,
                le.fecha_preferida_desde    AS "fechaPreferidaDesde",
                COUNT(*) OVER()             AS total_rows
            FROM lista_espera_cita le
            INNER JOIN paciente pac ON pac.id = le.paciente_id
            INNER JOIN tercero tp ON tp.id = pac.tercero_id
            LEFT JOIN especialidad esp ON esp.id = le.especialidad_id
            WHERE le.empresa_id = :empresa_id AND le.sede_id = :sede_id
              AND le.activo = true AND le.estado = 'ACTIVA'
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (tp.nombre_completo ILIKE :search OR tp.numero_documento ILIKE :search)");
            params.addValue("search", "%" + search + "%");
        }

        String orderBy = request.getOrder_by() != null ? request.getOrder_by() : "le.prioridad";
        String order   = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<WaitListTableDto> result = MapperRepository.mapListToDtoList(rows, WaitListTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();
        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }

    public boolean existsActivaForPaciente(Long pacienteId, Long especialidadId, Long empresaId, Long sedeId) {
        String sql = """
            SELECT COUNT(*) FROM lista_espera_cita
            WHERE paciente_id = :paciente_id AND especialidad_id = :especialidad_id
              AND empresa_id = :empresa_id AND sede_id = :sede_id
              AND estado = 'ACTIVA' AND activo = true
        """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
            .addValue("paciente_id", pacienteId)
            .addValue("especialidad_id", especialidadId)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId), Long.class);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> findActivasByEspecialidad(Long especialidadId, Long empresaId, Long sedeId) {
        String sql = """
            SELECT le.id, le.paciente_id, le.servicio_salud_id, le.especialidad_id, le.observaciones
            FROM lista_espera_cita le
            WHERE le.especialidad_id = :especialidad_id AND le.empresa_id = :empresa_id
              AND le.sede_id = :sede_id AND le.estado = 'ACTIVA' AND le.activo = true
            ORDER BY le.prioridad ASC, le.created_at ASC
        """;
        return jdbc.query(sql, new MapSqlParameterSource()
            .addValue("especialidad_id", especialidadId)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId), new ColumnMapRowMapper());
    }
}

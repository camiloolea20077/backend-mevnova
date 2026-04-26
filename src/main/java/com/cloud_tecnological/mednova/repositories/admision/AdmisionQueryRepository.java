package com.cloud_tecnological.mednova.repositories.admision;

import com.cloud_tecnological.mednova.dto.admision.AdmisionResponseDto;
import com.cloud_tecnological.mednova.dto.admision.AdmisionTableDto;
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
public class AdmisionQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AdmisionQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoAdmisionIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_admision WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public String findEstadoAdmisionCodigoById(Long estadoId) {
        String sql = "SELECT codigo FROM estado_admision WHERE id = :id LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("id", estadoId), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return (String) rows.get(0).get("codigo");
    }

    public String findTipoAdmisionCodigoById(Long tipoId) {
        String sql = "SELECT codigo FROM tipo_admision WHERE id = :id LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("id", tipoId), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return (String) rows.get(0).get("codigo");
    }

    public boolean existsPacienteInEmpresa(Long pacienteId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM paciente
            WHERE id = :id AND empresa_id = :empresa_id AND deleted_at IS NULL
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", pacienteId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsPagadorInEmpresa(Long pagadorId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM pagador
            WHERE id = :id AND empresa_id = :empresa_id AND activo = true AND deleted_at IS NULL
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", pagadorId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsContratoInEmpresa(Long contratoId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM contrato
            WHERE id = :id AND empresa_id = :empresa_id AND activo = true AND deleted_at IS NULL
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", contratoId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsTerceroInEmpresa(Long terceroId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM tercero
            WHERE id = :id AND empresa_id = :empresa_id AND deleted_at IS NULL
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", terceroId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public boolean existsAdmisionAbierta(Long pacienteId, Long tipoAdmisionId, Long sedeId, Long empresaId) {
        String sql = """
            SELECT COUNT(*) FROM admision a
            INNER JOIN estado_admision ea ON ea.id = a.estado_admision_id
            WHERE a.paciente_id = :paciente_id
              AND a.tipo_admision_id = :tipo_admision_id
              AND a.sede_id = :sede_id
              AND a.empresa_id = :empresa_id
              AND ea.codigo NOT IN ('EGRESADO','CANCELADO')
              AND a.deleted_at IS NULL
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("paciente_id", pacienteId)
            .addValue("tipo_admision_id", tipoAdmisionId)
            .addValue("sede_id", sedeId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public String generateNumeroAdmision(Long empresaId, Long sedeId) {
        String sql = """
            SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(numero_admision, '[^0-9]', '', 'g') AS BIGINT)), 0) + 1
            FROM admision
            WHERE empresa_id = :empresa_id AND sede_id = :sede_id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);
        Long next = jdbc.queryForObject(sql, params, Long.class);
        return String.format("ADM-%06d", next);
    }

    public Optional<AdmisionResponseDto> findActiveById(Long id, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                a.id,
                a.empresa_id,
                a.sede_id,
                a.numero_admision,
                a.paciente_id,
                t.nombre_completo     AS patient_name,
                t.numero_documento    AS document_number,
                a.tipo_admision_id,
                ta.nombre             AS admission_type_name,
                a.estado_admision_id,
                ea.nombre             AS status_name,
                ea.codigo             AS status_code,
                a.origen_atencion_id,
                oa.nombre             AS care_origin_name,
                a.pagador_id,
                tp.nombre_completo    AS payer_name,
                a.contrato_id,
                a.acompanante_id,
                a.motivo_ingreso,
                a.fecha_admision,
                a.fecha_egreso,
                a.tipo_egreso,
                a.observaciones,
                a.activo,
                a.created_at,
                at2.id                AS attention_id,
                eat.codigo            AS attention_status_code,
                at2.nivel_triage
            FROM admision a
            INNER JOIN paciente p        ON p.id = a.paciente_id
            INNER JOIN tercero t         ON t.id = p.tercero_id AND t.deleted_at IS NULL
            INNER JOIN tipo_admision ta  ON ta.id = a.tipo_admision_id
            INNER JOIN estado_admision ea ON ea.id = a.estado_admision_id
            INNER JOIN origen_atencion oa ON oa.id = a.origen_atencion_id
            INNER JOIN pagador pag       ON pag.id = a.pagador_id
            INNER JOIN tercero tp        ON tp.id = pag.tercero_id
            LEFT JOIN atencion at2       ON at2.admision_id = a.id AND at2.deleted_at IS NULL
                                       AND at2.id = (SELECT MIN(id) FROM atencion WHERE admision_id = a.id AND deleted_at IS NULL)
            LEFT JOIN estado_atencion eat ON eat.id = at2.estado_atencion_id
            WHERE a.id = :id
              AND a.empresa_id = :empresa_id
              AND a.sede_id = :sede_id
              AND a.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<AdmisionResponseDto> mapped = MapperRepository.mapListToDtoList(rows, AdmisionResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public boolean hasOpenAttentions(Long admisionId) {
        String sql = """
            SELECT COUNT(*) FROM atencion at
            INNER JOIN estado_atencion eat ON eat.id = at.estado_atencion_id
            WHERE at.admision_id = :admision_id
              AND eat.codigo != 'CERRADA'
              AND at.deleted_at IS NULL
        """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource("admision_id", admisionId), Long.class);
        return count != null && count > 0;
    }

    public PageImpl<AdmisionTableDto> listActive(PageableDto<?> request, Long empresaId, Long sedeId) {
        int pageNumber = request.getPage() != null ? request.getPage().intValue() : 0;
        int pageSize = request.getRows() != null ? request.getRows().intValue() : 10;
        String search = request.getSearch() != null ? request.getSearch().trim() : null;

        StringBuilder sql = new StringBuilder("""
            SELECT
                a.id,
                a.numero_admision,
                t.nombre_completo     AS patient_name,
                t.numero_documento    AS document_number,
                ta.nombre             AS admission_type_name,
                ea.nombre             AS status_name,
                ea.codigo             AS status_code,
                tp.nombre_completo    AS payer_name,
                a.fecha_admision,
                at2.nivel_triage,
                at2.id                AS attention_id,
                COUNT(*) OVER()       AS total_rows
            FROM admision a
            INNER JOIN paciente p         ON p.id = a.paciente_id
            INNER JOIN tercero t          ON t.id = p.tercero_id AND t.deleted_at IS NULL
            INNER JOIN tipo_admision ta   ON ta.id = a.tipo_admision_id
            INNER JOIN estado_admision ea ON ea.id = a.estado_admision_id
            INNER JOIN pagador pag        ON pag.id = a.pagador_id
            INNER JOIN tercero tp         ON tp.id = pag.tercero_id
            LEFT JOIN atencion at2        ON at2.admision_id = a.id AND at2.deleted_at IS NULL
                                        AND at2.id = (SELECT MIN(id) FROM atencion WHERE admision_id = a.id AND deleted_at IS NULL)
            WHERE a.empresa_id = :empresa_id
              AND a.sede_id = :sede_id
              AND ea.codigo NOT IN ('EGRESADO','CANCELADO')
              AND a.deleted_at IS NULL
        """);

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        if (search != null && !search.isEmpty()) {
            sql.append("""
                AND (
                    unaccent(LOWER(t.nombre_completo)) ILIKE unaccent(LOWER(:search))
                    OR t.numero_documento = :exactSearch
                    OR a.numero_admision ILIKE :search
                )
            """);
            params.addValue("search", "%" + search + "%");
            params.addValue("exactSearch", search);
        }

        String orderBy = request.getOrder_by() != null ? request.getOrder_by() : "a.fecha_admision";
        String order = "DESC".equalsIgnoreCase(request.getOrder()) ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(orderBy).append(" ").append(order);
        sql.append(" OFFSET :offset LIMIT :limit");
        params.addValue("offset", (long) pageNumber * pageSize);
        params.addValue("limit", pageSize);

        List<Map<String, Object>> rows = jdbc.query(sql.toString(), params, new ColumnMapRowMapper());
        List<AdmisionTableDto> result = MapperRepository.mapListToDtoList(rows, AdmisionTableDto.class);
        long count = rows.isEmpty() ? 0 : ((Number) rows.get(0).get("total_rows")).longValue();

        return new PageImpl<>(result, PageRequest.of(pageNumber, pageSize), count);
    }
}

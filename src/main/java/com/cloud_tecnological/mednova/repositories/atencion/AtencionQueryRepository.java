package com.cloud_tecnological.mednova.repositories.atencion;

import com.cloud_tecnological.mednova.dto.atencion.AtencionResponseDto;
import com.cloud_tecnological.mednova.dto.atencion.ColaUrgenciasDto;
import com.cloud_tecnological.mednova.dto.atencion.ConsolaAtencionDto;
import com.cloud_tecnological.mednova.util.MapperRepository;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AtencionQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AtencionQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long findEstadoAtencionIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_atencion WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public String findEstadoAtencionCodigoById(Long estadoId) {
        String sql = "SELECT codigo FROM estado_atencion WHERE id = :id LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
                new MapSqlParameterSource("id", estadoId), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return (String) rows.get(0).get("codigo");
    }

    public String generateNumeroAtencion(Long empresaId, Long sedeId) {
        String sql = """
            SELECT COALESCE(MAX(CAST(REGEXP_REPLACE(numero_atencion, '[^0-9]', '', 'g') AS BIGINT)), 0) + 1
            FROM atencion
            WHERE empresa_id = :empresa_id AND sede_id = :sede_id
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);
        Long next = jdbc.queryForObject(sql, params, Long.class);
        return String.format("ATE-%06d", next);
    }

    public Optional<AtencionResponseDto> findActiveById(Long id, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                at.id,
                at.admision_id,
                at.numero_atencion,
                at.estado_atencion_id,
                eat.codigo    AS status_code,
                eat.nombre    AS status_name,
                at.nivel_triage,
                at.motivo_consulta,
                at.fecha_inicio,
                at.fecha_cierre,
                at.tension_sistolica,
                at.tension_diastolica,
                at.frecuencia_cardiaca,
                at.frecuencia_respiratoria,
                at.temperatura,
                at.saturacion_oxigeno,
                at.peso,
                at.talla,
                at.glucometria,
                at.observaciones,
                at.created_at
            FROM atencion at
            INNER JOIN estado_atencion eat ON eat.id = at.estado_atencion_id
            WHERE at.id = :id
              AND at.empresa_id = :empresa_id
              AND at.sede_id = :sede_id
              AND at.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<AtencionResponseDto> mapped = MapperRepository.mapListToDtoList(rows, AtencionResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public Optional<AtencionResponseDto> findByAdmisionId(Long admisionId, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                at.id,
                at.admision_id,
                at.numero_atencion,
                at.estado_atencion_id,
                eat.codigo    AS status_code,
                eat.nombre    AS status_name,
                at.nivel_triage,
                at.motivo_consulta,
                at.fecha_inicio,
                at.fecha_cierre,
                at.tension_sistolica,
                at.tension_diastolica,
                at.frecuencia_cardiaca,
                at.frecuencia_respiratoria,
                at.temperatura,
                at.saturacion_oxigeno,
                at.peso,
                at.talla,
                at.glucometria,
                at.observaciones,
                at.created_at
            FROM atencion at
            INNER JOIN estado_atencion eat ON eat.id = at.estado_atencion_id
            WHERE at.admision_id = :admision_id
              AND at.empresa_id = :empresa_id
              AND at.sede_id = :sede_id
              AND at.deleted_at IS NULL
            ORDER BY at.id ASC
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("admision_id", admisionId)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<AtencionResponseDto> mapped = MapperRepository.mapListToDtoList(rows, AtencionResponseDto.class);
        return Optional.of(mapped.get(0));
    }

    public Optional<ConsolaAtencionDto> findConsolaData(Long atencionId, Long empresaId, Long sedeId) {
        String sql = """
            SELECT
                at.id,
                at.admision_id,
                at.numero_atencion,
                a.numero_admision,
                at.estado_atencion_id,
                eat.codigo                   AS status_code,
                eat.nombre                   AS status_name,
                at.nivel_triage,
                at.conducta,
                at.fecha_inicio,
                at.fecha_cierre,
                at.tension_sistolica,
                at.tension_diastolica,
                at.frecuencia_cardiaca,
                at.frecuencia_respiratoria,
                at.temperatura,
                at.saturacion_oxigeno,
                at.peso,
                at.talla,
                at.glucometria,
                at.motivo_consulta,
                at.enfermedad_actual,
                at.antecedentes,
                at.examen_fisico,
                at.analisis,
                at.plan,
                at.observaciones,
                p.id                         AS paciente_id,
                t.nombre_completo            AS patient_name,
                t.numero_documento           AS document_number,
                td.nombre                    AS document_type,
                s.codigo                     AS sex,
                EXTRACT(YEAR FROM age(t.fecha_nacimiento))::int AS age,
                p.alergias_conocidas         AS alergias,
                p.observaciones_clinicas,
                a.pagador_id,
                COALESCE(tp.razon_social, tp.nombre_completo) AS payer_name,
                a.contrato_id
            FROM atencion at
            INNER JOIN estado_atencion eat ON eat.id = at.estado_atencion_id
            INNER JOIN admision a          ON a.id   = at.admision_id AND a.empresa_id = :empresa_id
            INNER JOIN paciente p          ON p.id   = a.paciente_id  AND p.empresa_id = :empresa_id
            INNER JOIN tercero t           ON t.id   = p.tercero_id   AND t.deleted_at IS NULL
            LEFT  JOIN tipo_documento td   ON td.id  = t.tipo_documento_id
            LEFT  JOIN sexo s              ON s.id   = t.sexo_id
            LEFT  JOIN pagador pg          ON pg.id  = a.pagador_id   AND pg.empresa_id = :empresa_id
            LEFT  JOIN tercero tp          ON tp.id  = pg.tercero_id  AND tp.deleted_at IS NULL
            WHERE at.id         = :id
              AND at.empresa_id = :empresa_id
              AND at.sede_id    = :sede_id
              AND at.deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", atencionId)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        List<ConsolaAtencionDto> mapped = MapperRepository.mapListToDtoList(rows, ConsolaAtencionDto.class);
        return Optional.of(mapped.get(0));
    }

    public boolean existsPrincipalDiagnosis(Long atencionId, Long empresaId) {
        String sql = """
            SELECT COUNT(*)
            FROM diagnostico_atencion
            WHERE atencion_id      = :atencion_id
              AND empresa_id       = :empresa_id
              AND tipo_diagnostico = 'PRINCIPAL'
              AND activo           = true
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("atencion_id", atencionId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public Long findAdmisionIdByAtencion(Long atencionId, Long empresaId, Long sedeId) {
        String sql = """
            SELECT admision_id FROM atencion
            WHERE id = :id AND empresa_id = :empresa_id AND sede_id = :sede_id AND deleted_at IS NULL
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", atencionId)
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("admision_id")).longValue();
    }

    public Long findEstadoAdmisionIdByCodigo(String codigo) {
        String sql = "SELECT id FROM estado_admision WHERE codigo = :codigo AND activo = true LIMIT 1";
        List<Map<String, Object>> rows = jdbc.query(sql,
            new MapSqlParameterSource("codigo", codigo), new ColumnMapRowMapper());
        if (rows.isEmpty()) return null;
        return ((Number) rows.get(0).get("id")).longValue();
    }

    public Optional<Long> findProfesionalByUsuario(Long usuarioId, Long empresaId) {
        String sql = """
            SELECT ps.id
            FROM profesional_salud ps
            INNER JOIN usuario u ON u.tercero_id = ps.tercero_id
            WHERE u.id           = :usuario_id
              AND ps.empresa_id  = :empresa_id
              AND ps.activo      = true
            LIMIT 1
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("usuario_id", usuarioId)
            .addValue("empresa_id", empresaId);
        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(((Number) rows.get(0).get("id")).longValue());
    }

    public boolean existsConfirmedPrincipalDiagnosis(Long atencionId, Long empresaId) {
        String sql = """
            SELECT COUNT(*)
            FROM diagnostico_atencion
            WHERE atencion_id      = :atencion_id
              AND empresa_id       = :empresa_id
              AND tipo_diagnostico = 'PRINCIPAL'
              AND es_confirmado    = true
              AND activo           = true
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("atencion_id", atencionId)
            .addValue("empresa_id", empresaId);
        Long count = jdbc.queryForObject(sql, params, Long.class);
        return count != null && count > 0;
    }

    public List<ColaUrgenciasDto> findColaUrgencias(Long empresaId, Long sedeId) {
        // SLA por nivel de triage (minutos): I=0, II=10, III=30, IV=60, V=120
        String sql = """
            SELECT
                a.id            AS admision_id,
                at2.id          AS attention_id,
                a.numero_admision,
                t.nombre_completo  AS patient_name,
                t.numero_documento AS document_number,
                at2.nivel_triage,
                a.fecha_admision,
                EXTRACT(EPOCH FROM (NOW() - a.fecha_admision))::BIGINT / 60 AS wait_minutes,
                CASE at2.nivel_triage
                    WHEN 'I'   THEN 0
                    WHEN 'II'  THEN 10
                    WHEN 'III' THEN 30
                    WHEN 'IV'  THEN 60
                    WHEN 'V'   THEN 120
                    ELSE 120
                END             AS sla_minutes,
                CASE WHEN
                    EXTRACT(EPOCH FROM (NOW() - a.fecha_admision)) / 60 >
                    CASE at2.nivel_triage
                        WHEN 'I'   THEN 0
                        WHEN 'II'  THEN 10
                        WHEN 'III' THEN 30
                        WHEN 'IV'  THEN 60
                        WHEN 'V'   THEN 120
                        ELSE 120
                    END
                THEN true ELSE false END AS beyond_sla,
                eat.codigo      AS attention_status_code,
                eat.nombre      AS attention_status_name,
                at2.motivo_consulta
            FROM admision a
            INNER JOIN tipo_admision ta    ON ta.id = a.tipo_admision_id AND ta.codigo = 'URGENCIAS'
            INNER JOIN estado_admision ea  ON ea.id = a.estado_admision_id AND ea.codigo NOT IN ('EGRESADO','CANCELADO')
            INNER JOIN paciente p          ON p.id = a.paciente_id
            INNER JOIN tercero t           ON t.id = p.tercero_id AND t.deleted_at IS NULL
            INNER JOIN atencion at2        ON at2.admision_id = a.id AND at2.deleted_at IS NULL
                                         AND at2.id = (SELECT MIN(id) FROM atencion WHERE admision_id = a.id AND deleted_at IS NULL)
            INNER JOIN estado_atencion eat ON eat.id = at2.estado_atencion_id
                                         AND eat.codigo IN ('EN_TRIAGE','PENDIENTE')
            WHERE a.empresa_id = :empresa_id
              AND a.sede_id    = :sede_id
              AND a.deleted_at IS NULL
            ORDER BY
                CASE at2.nivel_triage
                    WHEN 'I'   THEN 1
                    WHEN 'II'  THEN 2
                    WHEN 'III' THEN 3
                    WHEN 'IV'  THEN 4
                    WHEN 'V'   THEN 5
                    ELSE 6
                END ASC,
                a.fecha_admision ASC
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("empresa_id", empresaId)
            .addValue("sede_id", sedeId);

        List<Map<String, Object>> rows = jdbc.query(sql, params, new ColumnMapRowMapper());
        return MapperRepository.mapListToDtoList(rows, ColaUrgenciasDto.class);
    }
}

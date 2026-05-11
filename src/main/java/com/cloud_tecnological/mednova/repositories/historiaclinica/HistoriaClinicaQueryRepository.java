package com.cloud_tecnological.mednova.repositories.historiaclinica;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.cloud_tecnological.mednova.dto.historiaclinica.HCAnamnesisDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCAttachmentDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCEpisodeDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCHeaderDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCMedicationDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCNoteDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCOrderDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCScaleDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCSummaryDto;
import com.cloud_tecnological.mednova.dto.historiaclinica.HCTimelineEventDto;
import com.cloud_tecnological.mednova.util.GlobalException;

@Repository
public class HistoriaClinicaQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public HistoriaClinicaQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── Validación de paciente ──────────────────────────────────────────────

    public boolean pacienteExistsInEmpresa(Long paciente_id, Long empresa_id) {
        String sql = "SELECT COUNT(*) FROM paciente WHERE id = :id AND empresa_id = :empresa_id AND deleted_at IS NULL";
        Long c = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("id", paciente_id)
                .addValue("empresa_id", empresa_id), Long.class);
        return c != null && c > 0;
    }

    // ── CA1+CA4: Header ─────────────────────────────────────────────────────

    public Optional<HCHeaderDto> getHeader(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT p.id                                    AS paciente_id,
                       t.numero_documento,
                       td.codigo                               AS tipo_documento,
                       (t.primer_nombre || ' ' ||
                        COALESCE(t.segundo_nombre || ' ', '') ||
                        t.primer_apellido || ' ' ||
                        COALESCE(t.segundo_apellido, ''))      AS nombre_completo,
                       t.sexo,
                       t.fecha_nacimiento,
                       gs.nombre                               AS grupo_sanguineo,
                       fr.nombre                               AS factor_rh,
                       ga.nombre                               AS grupo_atencion,
                       p.alergias_conocidas
                FROM paciente p
                INNER JOIN tercero t              ON t.id = p.tercero_id
                LEFT  JOIN tipo_documento td      ON td.id = t.tipo_documento_id
                LEFT  JOIN grupo_sanguineo gs     ON gs.id = p.grupo_sanguineo_id
                LEFT  JOIN factor_rh fr           ON fr.id = p.factor_rh_id
                LEFT  JOIN grupo_atencion ga      ON ga.id = p.grupo_atencion_id
                WHERE p.id         = :id
                  AND p.empresa_id = :empresa_id
                  AND p.deleted_at IS NULL
                LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, new MapSqlParameterSource()
                .addValue("id", paciente_id)
                .addValue("empresa_id", empresa_id), new ColumnMapRowMapper());
        if (rows.isEmpty()) return Optional.empty();
        Map<String, Object> r = rows.get(0);

        List<String> allergies = findAllergies(paciente_id, empresa_id);
        LocalDate birth = toLocalDate(r.get("fecha_nacimiento"));
        Integer age = birth == null ? null : Period.between(birth, LocalDate.now()).getYears();

        return Optional.of(HCHeaderDto.builder()
                .patientId(toLong(r.get("paciente_id")))
                .documentNumber((String) r.get("numero_documento"))
                .documentType((String) r.get("tipo_documento"))
                .fullName(((String) r.getOrDefault("nombre_completo", "")).trim())
                .sex((String) r.get("sexo"))
                .birthDate(birth)
                .ageYears(age)
                .bloodGroup((String) r.get("grupo_sanguineo"))
                .rhFactor((String) r.get("factor_rh"))
                .careGroup((String) r.get("grupo_atencion"))
                .knownAllergies((String) r.get("alergias_conocidas"))
                .highlightedAllergies(allergies)
                .hasAllergies(!allergies.isEmpty()
                        || (r.get("alergias_conocidas") != null
                            && !((String) r.get("alergias_conocidas")).isBlank()))
                .build());
    }

    private List<String> findAllergies(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT ap.descripcion
                FROM antecedente_personal ap
                INNER JOIN tipo_antecedente ta ON ta.id = ap.tipo_antecedente_id
                WHERE ap.paciente_id = :pid
                  AND ap.empresa_id  = :empresa_id
                  AND ap.deleted_at IS NULL
                  AND ap.es_activo   = true
                  AND ta.codigo      = 'ALERGICO'
                ORDER BY ap.created_at DESC
                """;
        try {
            return jdbc.queryForList(sql, new MapSqlParameterSource()
                    .addValue("pid", paciente_id)
                    .addValue("empresa_id", empresa_id), String.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    // ── Summary ──────────────────────────────────────────────────────────────

    public HCSummaryDto getSummary(Long paciente_id, Long empresa_id) {
        String sqlDiag = """
                SELECT DISTINCT (COALESCE(cd.codigo,'') || ' - ' || COALESCE(cd.descripcion,'')) AS dx
                FROM diagnostico_atencion da
                INNER JOIN atencion a              ON a.id  = da.atencion_id
                INNER JOIN admision  adm           ON adm.id = a.admision_id
                LEFT  JOIN catalogo_diagnostico cd ON cd.id = da.catalogo_diagnostico_id
                WHERE adm.paciente_id = :pid
                  AND a.empresa_id    = :empresa_id
                  AND da.deleted_at  IS NULL
                ORDER BY 1
                """;
        List<String> diagnoses = safeListString(sqlDiag, paciente_id, empresa_id);

        String sqlMed = """
                SELECT (mh.nombre_medicamento || ' ' || COALESCE(mh.dosis,'')) AS m
                FROM medicacion_habitual mh
                WHERE mh.paciente_id = :pid
                  AND mh.empresa_id  = :empresa_id
                  AND mh.es_activo   = true
                  AND mh.deleted_at  IS NULL
                ORDER BY mh.nombre_medicamento
                """;
        List<String> meds = safeListString(sqlMed, paciente_id, empresa_id);

        String sqlLastEnc = """
                SELECT MAX(a.fecha_inicio) AS last_at
                FROM atencion a
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE adm.paciente_id = :pid
                  AND a.empresa_id    = :empresa_id
                  AND a.deleted_at IS NULL
                """;
        LocalDateTime lastEnc = querySingleTimestamp(sqlLastEnc, paciente_id, empresa_id);

        String sqlNextApp = """
                SELECT MIN(c.fecha_cita) AS next_at
                FROM cita c
                WHERE c.paciente_id = :pid
                  AND c.empresa_id  = :empresa_id
                  AND c.fecha_cita  >= current_timestamp
                  AND c.deleted_at IS NULL
                """;
        LocalDateTime nextApp = querySingleTimestamp(sqlNextApp, paciente_id, empresa_id);

        return HCSummaryDto.builder()
                .activeDiagnoses(diagnoses)
                .habitualMedications(meds)
                .lastEncounterAt(lastEnc)
                .nextAppointmentAt(nextApp)
                .build();
    }

    // ── Episodes ─────────────────────────────────────────────────────────────

    public List<HCEpisodeDto> getEpisodes(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT adm.id,
                       adm.numero_admision,
                       adm.fecha_admision,
                       adm.fecha_egreso,
                       adm.tipo_egreso,
                       (SELECT COUNT(*)
                        FROM atencion a
                        WHERE a.admision_id = adm.id
                          AND a.deleted_at IS NULL) AS atenciones
                FROM admision adm
                WHERE adm.paciente_id = :pid
                  AND adm.empresa_id  = :empresa_id
                  AND adm.deleted_at IS NULL
                ORDER BY adm.fecha_admision DESC
                """;
        List<Map<String, Object>> rows = jdbc.query(sql, params(paciente_id, empresa_id), new ColumnMapRowMapper());
        List<HCEpisodeDto> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Long admId = toLong(r.get("id"));
            list.add(HCEpisodeDto.builder()
                    .admissionId(admId)
                    .admissionNumber((String) r.get("numero_admision"))
                    .admissionDate(toLocalDateTime(r.get("fecha_admision")))
                    .dischargeDate(toLocalDateTime(r.get("fecha_egreso")))
                    .dischargeType((String) r.get("tipo_egreso"))
                    .encounterCount(toInteger(r.get("atenciones")))
                    .diagnoses(getEpisodeDiagnoses(admId, empresa_id))
                    .build());
        }
        return list;
    }

    private List<String> getEpisodeDiagnoses(Long admision_id, Long empresa_id) {
        String sql = """
                SELECT DISTINCT (COALESCE(cd.codigo,'') || ' - ' || COALESCE(cd.descripcion,'')) AS dx
                FROM diagnostico_atencion da
                INNER JOIN atencion a              ON a.id  = da.atencion_id
                LEFT  JOIN catalogo_diagnostico cd ON cd.id = da.catalogo_diagnostico_id
                WHERE a.admision_id = :aid
                  AND a.empresa_id  = :empresa_id
                  AND da.deleted_at IS NULL
                ORDER BY 1
                """;
        try {
            return jdbc.queryForList(sql, new MapSqlParameterSource()
                    .addValue("aid", admision_id)
                    .addValue("empresa_id", empresa_id), String.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    // ── Anamnesis ────────────────────────────────────────────────────────────

    public HCAnamnesisDto getAnamnesis(Long paciente_id, Long empresa_id) {
        Map<String, List<String>> personalByType = new LinkedHashMap<>();
        try {
            String sql = """
                    SELECT ta.codigo AS tipo, ap.descripcion AS d
                    FROM antecedente_personal ap
                    INNER JOIN tipo_antecedente ta ON ta.id = ap.tipo_antecedente_id
                    WHERE ap.paciente_id = :pid
                      AND ap.empresa_id  = :empresa_id
                      AND ap.deleted_at IS NULL
                    ORDER BY ta.codigo, ap.created_at DESC
                    """;
            for (Map<String, Object> row : jdbc.query(sql, params(paciente_id, empresa_id), new ColumnMapRowMapper())) {
                String tipo = (String) row.get("tipo");
                personalByType.computeIfAbsent(tipo, k -> new ArrayList<>()).add((String) row.get("d"));
            }
        } catch (Exception ignore) { /* tabla puede no estar en algún ambiente */ }

        return HCAnamnesisDto.builder()
                .personalHistoryByType(personalByType)
                .familyHistory(safeListString("""
                        SELECT (af.parentesco || ': ' || af.descripcion)
                        FROM antecedente_familiar af
                        WHERE af.paciente_id = :pid AND af.empresa_id = :empresa_id AND af.deleted_at IS NULL
                        ORDER BY af.created_at DESC
                        """, paciente_id, empresa_id))
                .habits(safeListString("""
                        SELECT (h.tipo_habito || ': ' || h.estado || ' - ' || h.descripcion)
                        FROM habito_paciente h
                        WHERE h.paciente_id = :pid AND h.empresa_id = :empresa_id AND h.deleted_at IS NULL
                        ORDER BY h.tipo_habito
                        """, paciente_id, empresa_id))
                .vaccines(safeListString("""
                        SELECT (v.nombre_vacuna || ' dosis ' || v.dosis || ' - ' || v.fecha_aplicacion)
                        FROM vacuna_paciente v
                        WHERE v.paciente_id = :pid AND v.empresa_id = :empresa_id AND v.deleted_at IS NULL
                        ORDER BY v.fecha_aplicacion DESC
                        """, paciente_id, empresa_id))
                .habitualMedications(safeListString("""
                        SELECT (mh.nombre_medicamento || ' ' || COALESCE(mh.dosis,''))
                        FROM medicacion_habitual mh
                        WHERE mh.paciente_id = :pid AND mh.empresa_id = :empresa_id AND mh.deleted_at IS NULL
                        ORDER BY mh.nombre_medicamento
                        """, paciente_id, empresa_id))
                .build();
    }

    // ── Notes ────────────────────────────────────────────────────────────────

    public List<HCNoteDto> getNotes(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT 'NOTA_ENFERMERIA' AS source,
                       n.atencion_id,
                       n.tipo_nota,
                       n.fecha_nota,
                       n.firmada,
                       (te.primer_nombre || ' ' || te.primer_apellido) AS profesional,
                       n.contenido
                FROM nota_enfermeria n
                LEFT JOIN profesional_salud ps ON ps.id = n.profesional_id
                LEFT JOIN tercero           te ON te.id = ps.tercero_id
                WHERE n.paciente_id = :pid
                  AND n.empresa_id  = :empresa_id
                  AND n.deleted_at IS NULL
                ORDER BY n.fecha_nota DESC
                """;
        List<HCNoteDto> notes = new ArrayList<>();
        try {
            for (Map<String, Object> r : jdbc.query(sql, params(paciente_id, empresa_id), new ColumnMapRowMapper())) {
                String content = (String) r.get("contenido");
                notes.add(HCNoteDto.builder()
                        .source((String) r.get("source"))
                        .encounterId(toLong(r.get("atencion_id")))
                        .noteType((String) r.get("tipo_nota"))
                        .noteAt(toLocalDateTime(r.get("fecha_nota")))
                        .professionalName((String) r.get("profesional"))
                        .contentPreview(preview(content))
                        .signed((Boolean) r.get("firmada"))
                        .build());
            }
        } catch (Exception ignore) { /* tabla puede no existir */ }
        return notes;
    }

    // ── Orders ───────────────────────────────────────────────────────────────

    public List<HCOrderDto> getOrders(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT oc.id,
                       oc.numero_orden,
                       oc.atencion_id,
                       oc.estado,
                       oc.created_at,
                       (SELECT COUNT(*) FROM detalle_orden_clinica d
                         WHERE d.orden_clinica_id = oc.id AND d.deleted_at IS NULL) AS items
                FROM orden_clinica oc
                INNER JOIN atencion a   ON a.id   = oc.atencion_id
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE adm.paciente_id = :pid
                  AND oc.empresa_id   = :empresa_id
                  AND oc.deleted_at IS NULL
                ORDER BY oc.created_at DESC
                """;
        List<HCOrderDto> list = new ArrayList<>();
        try {
            for (Map<String, Object> r : jdbc.query(sql, params(paciente_id, empresa_id), new ColumnMapRowMapper())) {
                list.add(HCOrderDto.builder()
                        .orderId(toLong(r.get("id")))
                        .orderNumber((String) r.get("numero_orden"))
                        .encounterId(toLong(r.get("atencion_id")))
                        .status((String) r.get("estado"))
                        .createdAt(toLocalDateTime(r.get("created_at")))
                        .itemCount(toInteger(r.get("items")))
                        .build());
            }
        } catch (Exception ignore) { }
        return list;
    }

    // ── Medications + MAR ───────────────────────────────────────────────────

    public HCMedicationDto getMedications(Long paciente_id, Long empresa_id) {
        List<String> prescriptions = safeListString("""
                SELECT (pr.numero_prescripcion || ' (' || pr.estado || ')')
                FROM prescripcion pr
                INNER JOIN atencion a   ON a.id   = pr.atencion_id
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE adm.paciente_id = :pid
                  AND pr.empresa_id   = :empresa_id
                  AND pr.deleted_at IS NULL
                ORDER BY pr.created_at DESC
                """, paciente_id, empresa_id);

        List<String> dispensations = safeListString("""
                SELECT (d.numero_dispensacion || ' - ' || d.estado)
                FROM dispensacion d
                WHERE d.paciente_id = :pid
                  AND d.empresa_id  = :empresa_id
                  AND d.deleted_at IS NULL
                ORDER BY d.fecha_dispensacion DESC
                """, paciente_id, empresa_id);

        long programadas = 0, administradas = 0, omitidas = 0;
        LocalDateTime lastAdm = null;
        try {
            String sql = """
                    SELECT estado, COUNT(*) AS c, MAX(fecha_administracion) AS last_adm
                    FROM administracion_medicamento
                    WHERE paciente_id = :pid AND empresa_id = :empresa_id AND deleted_at IS NULL
                    GROUP BY estado
                    """;
            for (Map<String, Object> r : jdbc.query(sql, params(paciente_id, empresa_id), new ColumnMapRowMapper())) {
                String est = (String) r.get("estado");
                long c = ((Number) r.get("c")).longValue();
                if ("PROGRAMADA".equals(est)) programadas = c;
                else if ("ADMINISTRADA".equals(est)) {
                    administradas = c;
                    lastAdm = toLocalDateTime(r.get("last_adm"));
                } else if ("OMITIDA".equals(est) || "RECHAZADA".equals(est)) omitidas += c;
            }
        } catch (Exception ignore) { }

        return HCMedicationDto.builder()
                .prescriptions(prescriptions)
                .dispensations(dispensations)
                .marProgrammed(programadas)
                .marAdministered(administradas)
                .marOmitted(omitidas)
                .lastAdministrationAt(lastAdm)
                .build();
    }

    // ── Scales ───────────────────────────────────────────────────────────────

    public List<HCScaleDto> getScales(Long paciente_id, Long empresa_id) {
        String sql = """
                SELECT id, atencion_id, tipo_escala, puntaje_total, riesgo, fecha_aplicacion
                FROM escala_clinica
                WHERE paciente_id = :pid AND empresa_id = :empresa_id AND deleted_at IS NULL
                ORDER BY fecha_aplicacion DESC
                """;
        List<HCScaleDto> list = new ArrayList<>();
        try {
            for (Map<String, Object> r : jdbc.query(sql, params(paciente_id, empresa_id), new ColumnMapRowMapper())) {
                list.add(HCScaleDto.builder()
                        .scaleId(toLong(r.get("id")))
                        .encounterId(toLong(r.get("atencion_id")))
                        .scaleType((String) r.get("tipo_escala"))
                        .totalScore(toInteger(r.get("puntaje_total")))
                        .risk((String) r.get("riesgo"))
                        .appliedAt(toLocalDateTime(r.get("fecha_aplicacion")))
                        .build());
            }
        } catch (Exception ignore) { }
        return list;
    }

    // ── Attachments ──────────────────────────────────────────────────────────

    public List<HCAttachmentDto> getAttachments(Long paciente_id, Long empresa_id) {

        String sql = """
                SELECT ac.id,
                    ac.tipo_documento,
                    ac.nombre_archivo,
                    ac.url_archivo,
                    ac.fecha_documento,
                    ac.created_at,
                    ac.es_confidencial
                FROM adjunto_clinico ac
                WHERE ac.paciente_id = :pid
                AND ac.empresa_id = :empresa_id
                AND ac.deleted_at IS NULL
                ORDER BY ac.created_at DESC
                """;

        try {

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("pid", paciente_id)
                    .addValue("empresa_id", empresa_id);

            return jdbc.query(sql, params, (rs, rowNum) ->
                    HCAttachmentDto.builder()
                            .attachmentId(rs.getLong("id"))
                            .documentType(rs.getString("tipo_documento"))
                            .fileName(rs.getString("nombre_archivo"))
                            .fileUrl(rs.getString("url_archivo"))
                            .documentDate(
                                    rs.getDate("fecha_documento") != null
                                            ? rs.getDate("fecha_documento").toLocalDate()
                                            : null
                            )
                            .createdAt(
                                    rs.getTimestamp("created_at") != null
                                            ? rs.getTimestamp("created_at").toLocalDateTime()
                                            : null
                            )
                            .isConfidential(rs.getBoolean("es_confidencial"))
                            .build()
            );

        } catch (Exception e) {

            log.error("Error consultando adjuntos clínicos. paciente_id={}, empresa_id={}",
                    paciente_id, empresa_id, e);

            throw new GlobalException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error consultando adjuntos clínicos"
            );
        }
    }
    // ── CA5: Timeline (unión cronológica de eventos) ────────────────────────

    public List<HCTimelineEventDto> getTimeline(Long paciente_id, Long empresa_id) {
        List<HCTimelineEventDto> events = new ArrayList<>();

        addEvents(events, """
                SELECT id AS ref, fecha_admision AS at, ('Admisión ' || numero_admision) AS summary
                FROM admision
                WHERE paciente_id = :pid AND empresa_id = :empresa_id AND deleted_at IS NULL
                """, paciente_id, empresa_id, "ADMISION");

        addEvents(events, """
                SELECT a.id AS ref, a.fecha_inicio AS at, ('Atención #' || a.id) AS summary
                FROM atencion a
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE adm.paciente_id = :pid AND a.empresa_id = :empresa_id AND a.deleted_at IS NULL
                """, paciente_id, empresa_id, "ATENCION");

        addEvents(events, """
                SELECT id AS ref, fecha_nota AS at, (tipo_nota || COALESCE(' - turno ' || turno, '')) AS summary
                FROM nota_enfermeria
                WHERE paciente_id = :pid AND empresa_id = :empresa_id AND deleted_at IS NULL
                """, paciente_id, empresa_id, "NOTA");

        addEvents(events, """
                SELECT oc.id AS ref, oc.created_at AS at, ('Orden ' || oc.numero_orden) AS summary
                FROM orden_clinica oc
                INNER JOIN atencion a   ON a.id   = oc.atencion_id
                INNER JOIN admision adm ON adm.id = a.admision_id
                WHERE adm.paciente_id = :pid AND oc.empresa_id = :empresa_id AND oc.deleted_at IS NULL
                """, paciente_id, empresa_id, "ORDEN");

        addEvents(events, """
                SELECT id AS ref, fecha_aplicacion AS at, (tipo_escala || ': ' || puntaje_total) AS summary
                FROM escala_clinica
                WHERE paciente_id = :pid AND empresa_id = :empresa_id AND deleted_at IS NULL
                """, paciente_id, empresa_id, "ESCALA");

        addEvents(events, """
                SELECT id AS ref, fecha_administracion AS at, ('MAR: dosis ' || estado) AS summary
                FROM administracion_medicamento
                WHERE paciente_id = :pid AND empresa_id = :empresa_id AND deleted_at IS NULL
                  AND fecha_administracion IS NOT NULL
                """, paciente_id, empresa_id, "MAR");

        addEvents(events, """
                SELECT id AS ref, fecha_egreso AS at, ('Epicrisis admisión ' || admision_id) AS summary
                FROM epicrisis
                WHERE paciente_id = :pid AND empresa_id = :empresa_id AND deleted_at IS NULL
                """, paciente_id, empresa_id, "EPICRISIS");

        addEvents(events, """
                SELECT id AS ref, created_at AS at, (tipo_documento || ': ' || nombre_archivo) AS summary
                FROM adjunto_clinico
                WHERE paciente_id = :pid AND empresa_id = :empresa_id AND deleted_at IS NULL
                """, paciente_id, empresa_id, "ADJUNTO");

        events.sort(Comparator.comparing(HCTimelineEventDto::getEventAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return events;
    }

    private void addEvents(List<HCTimelineEventDto> events, String sql,
                           Long paciente_id, Long empresa_id, String type) {
        try {
            for (Map<String, Object> r : jdbc.query(sql, params(paciente_id, empresa_id), new ColumnMapRowMapper())) {
                events.add(HCTimelineEventDto.builder()
                        .eventType(type)
                        .eventAt(toLocalDateTime(r.get("at")))
                        .referenceId(toLong(r.get("ref")))
                        .summary((String) r.get("summary"))
                        .build());
            }
        } catch (Exception ignore) { }
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private MapSqlParameterSource params(Long paciente_id, Long empresa_id) {
        return new MapSqlParameterSource()
                .addValue("pid", paciente_id)
                .addValue("empresa_id", empresa_id);
    }

    private List<String> safeListString(String sql, Long paciente_id, Long empresa_id) {
        try {
            return jdbc.queryForList(sql, params(paciente_id, empresa_id), String.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private LocalDateTime querySingleTimestamp(String sql, Long paciente_id, Long empresa_id) {
        try {
            List<Map<String, Object>> rows = jdbc.query(sql, params(paciente_id, empresa_id), new ColumnMapRowMapper());
            if (rows.isEmpty()) return null;
            return toLocalDateTime(rows.get(0).values().iterator().next());
        } catch (Exception e) {
            return null;
        }
    }

    private String preview(String content) {
        if (content == null) return null;
        return content.length() > 160 ? content.substring(0, 160) + "…" : content;
    }

    private Long toLong(Object v) { return v == null ? null : ((Number) v).longValue(); }
    private Integer toInteger(Object v) { return v == null ? null : ((Number) v).intValue(); }

    private LocalDate toLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        if (v instanceof Date d) return d.toLocalDate();
        return null;
    }

    private LocalDateTime toLocalDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

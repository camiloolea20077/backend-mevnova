package com.cloud_tecnological.mednova.services.impl;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.cloud_tecnological.mednova.entity.AuditoriaEntity;
import com.cloud_tecnological.mednova.repositories.auth.AuditoriaJpaRepository;
import com.cloud_tecnological.mednova.repositories.historiaclinica.HistoriaClinicaQueryRepository;
import com.cloud_tecnological.mednova.services.HistoriaClinicaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HistoriaClinicaServiceImpl implements HistoriaClinicaService {

    private final HistoriaClinicaQueryRepository query;
    private final AuditoriaJpaRepository auditoria;
    private final ObjectMapper objectMapper;

    public HistoriaClinicaServiceImpl(HistoriaClinicaQueryRepository query,
                                      AuditoriaJpaRepository auditoria,
                                      ObjectMapper objectMapper) {
        this.query = query;
        this.auditoria = auditoria;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public HCHeaderDto getHeader(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        HCHeaderDto dto = query.getHeader(patientId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
        audit(patientId, "HEADER", Map.of("hasAllergies", dto.isHasAllergies()));
        return dto;
    }

    @Override
    @Transactional
    public HCSummaryDto getSummary(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        HCSummaryDto dto = query.getSummary(patientId, empresa_id);
        audit(patientId, "SUMMARY", null);
        return dto;
    }

    @Override
    @Transactional
    public List<HCEpisodeDto> getEpisodes(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        List<HCEpisodeDto> list = query.getEpisodes(patientId, empresa_id);
        audit(patientId, "EPISODES", Map.of("count", list.size()));
        return list;
    }

    @Override
    @Transactional
    public HCAnamnesisDto getAnamnesis(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        HCAnamnesisDto dto = query.getAnamnesis(patientId, empresa_id);
        audit(patientId, "ANAMNESIS", null);
        return dto;
    }

    @Override
    @Transactional
    public List<HCNoteDto> getNotes(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        List<HCNoteDto> list = query.getNotes(patientId, empresa_id);
        audit(patientId, "NOTES", Map.of("count", list.size()));
        return list;
    }

    @Override
    @Transactional
    public List<HCOrderDto> getOrders(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        List<HCOrderDto> list = query.getOrders(patientId, empresa_id);
        audit(patientId, "ORDERS", Map.of("count", list.size()));
        return list;
    }

    @Override
    @Transactional
    public HCMedicationDto getMedications(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        HCMedicationDto dto = query.getMedications(patientId, empresa_id);
        audit(patientId, "MEDICATIONS", null);
        return dto;
    }

    @Override
    @Transactional
    public List<HCScaleDto> getScales(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        List<HCScaleDto> list = query.getScales(patientId, empresa_id);
        audit(patientId, "SCALES", Map.of("count", list.size()));
        return list;
    }

    @Override
    @Transactional
    public List<HCAttachmentDto> getAttachments(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        List<HCAttachmentDto> list = query.getAttachments(patientId, empresa_id);
        audit(patientId, "ATTACHMENTS", Map.of("count", list.size()));
        return list;
    }

    @Override
    @Transactional
    public List<HCTimelineEventDto> getTimeline(Long patientId) {
        Long empresa_id = enforceAccess(patientId);
        List<HCTimelineEventDto> list = query.getTimeline(patientId, empresa_id);
        audit(patientId, "TIMELINE", Map.of("events", list.size()));
        return list;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** CA2 (permiso) + CA1 (aislamiento por empresa). Devuelve empresa_id. */
    private Long enforceAccess(Long patientId) {
        Long empresa_id = TenantContext.getEmpresaId();

        if (!query.pacienteExistsInEmpresa(patientId, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }

        return empresa_id;
    }

    /** CA3: registra el acceso de lectura. */
    private void audit(Long patientId, String section, Map<String, Object> details) {
        try {
            AuditoriaEntity a = new AuditoriaEntity();
            a.setEmpresa_id(TenantContext.getEmpresaId());
            a.setSede_id(TenantContext.getSedeId());
            a.setUsuario_id(TenantContext.getUsuarioId());
            a.setTabla_afectada("historia_clinica");
            a.setRegistro_id(String.valueOf(patientId));
            a.setAccion("VIEW");
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("section", section);
            if (details != null) data.putAll(details);
            a.setDatos_despues(objectMapper.writeValueAsString(data));
            auditoria.save(a);
        } catch (Exception ignore) {
            // No bloquear la lectura si la auditoría falla.
        }
    }
}

package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.escalaclinica.CreateEscalaClinicaRequestDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaFilterParams;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaResponseDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaTableDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.UpdateEscalaClinicaRequestDto;
import com.cloud_tecnological.mednova.entity.EscalaClinicaEntity;
import com.cloud_tecnological.mednova.repositories.escalaclinica.EscalaClinicaJpaRepository;
import com.cloud_tecnological.mednova.repositories.escalaclinica.EscalaClinicaQueryRepository;
import com.cloud_tecnological.mednova.services.EscalaClinicaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EscalaClinicaServiceImpl implements EscalaClinicaService {

    private static final String RIESGO_BAJO     = "BAJO";
    private static final String RIESGO_MEDIO    = "MEDIO";
    private static final String RIESGO_ALTO     = "ALTO";
    private static final String RIESGO_MUY_ALTO = "MUY_ALTO";

    private final EscalaClinicaJpaRepository jpa;
    private final EscalaClinicaQueryRepository query;
    private final ObjectMapper objectMapper;

    public EscalaClinicaServiceImpl(EscalaClinicaJpaRepository jpa,
                                    EscalaClinicaQueryRepository query,
                                    ObjectMapper objectMapper) {
        this.jpa = jpa;
        this.query = query;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public EscalaClinicaResponseDto create(CreateEscalaClinicaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateAtencion(empresa_id, sede_id, request.getEncounterId());
        validateAtencionMatchesPaciente(request.getEncounterId(), request.getPatientId());
        validateProfesional(empresa_id, request.getProfessionalId());

        EscalaClinicaEntity entity = new EscalaClinicaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(sede_id);
        entity.setAtencion_id(request.getEncounterId());
        entity.setPaciente_id(request.getPatientId());
        entity.setProfesional_id(request.getProfessionalId());
        entity.setTipo_escala(request.getScaleType());
        entity.setFecha_aplicacion(request.getAppliedAt() == null ? LocalDateTime.now() : request.getAppliedAt());
        entity.setPuntaje_total(request.getTotalScore());
        entity.setInterpretacion(request.getInterpretation());
        // CA1: si el cliente no provee riesgo, intentar calcular por umbrales conocidos.
        String risk = request.getRisk();
        if (risk == null || risk.isBlank()) {
            risk = computeRisk(request.getScaleType(), request.getTotalScore());
        }
        entity.setRiesgo(risk);
        entity.setDetalle_escala(serializeJson(request.getScaleDetail()));
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);
        EscalaClinicaEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la escala creada"));
    }

    @Override
    @Transactional
    public EscalaClinicaResponseDto update(Long id, UpdateEscalaClinicaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        EscalaClinicaEntity entity = getValidEntity(id, empresa_id, sede_id);
        validateProfesional(empresa_id, request.getProfessionalId());

        entity.setProfesional_id(request.getProfessionalId());
        entity.setPuntaje_total(request.getTotalScore());
        entity.setInterpretacion(request.getInterpretation());
        String risk = request.getRisk();
        if (risk == null || risk.isBlank()) {
            risk = computeRisk(entity.getTipo_escala(), request.getTotalScore());
        }
        entity.setRiesgo(risk);
        if (request.getScaleDetail() != null) {
            entity.setDetalle_escala(serializeJson(request.getScaleDetail()));
        }
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la escala actualizada"));
    }

    @Override
    public EscalaClinicaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Escala clínica no encontrada"));
    }

    @Override
    public PageImpl<EscalaClinicaTableDto> list(PageableDto<EscalaClinicaFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.listEscalas(pageable, empresa_id, sede_id);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        EscalaClinicaEntity entity = getValidEntity(id, empresa_id, sede_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private EscalaClinicaEntity getValidEntity(Long id, Long empresa_id, Long sede_id) {
        EscalaClinicaEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Escala clínica no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id())
                || !sede_id.equals(entity.getSede_id())
                || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Escala clínica no encontrada");
        }
        return entity;
    }

    private void validatePaciente(Long empresa_id, Long paciente_id) {
        if (!query.pacienteExistsInEmpresa(paciente_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }
    }

    private void validateAtencion(Long empresa_id, Long sede_id, Long atencion_id) {
        if (!query.atencionExistsInTenant(atencion_id, empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Atención no encontrada");
        }
    }

    private void validateAtencionMatchesPaciente(Long atencion_id, Long paciente_id) {
        if (!query.atencionMatchesPaciente(atencion_id, paciente_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La atención no corresponde al paciente indicado");
        }
    }

    private void validateProfesional(Long empresa_id, Long profesional_id) {
        if (!query.profesionalActivoInEmpresa(profesional_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }
    }

    private String serializeJson(JsonNode node) {
        if (node == null || node.isNull()) return null;
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "scaleDetail no es JSON válido");
        }
    }

    /**
     * CA1: cálculo de riesgo por umbrales estandarizados para escalas conocidas.
     * Si la escala no tiene formula de riesgo (BARTHEL, LAWTON, KATZ, MINI_MENTAL, APGAR, OTRA),
     * se retorna null y el cliente puede enviarlo explícitamente.
     */
    private String computeRisk(String scaleType, Integer score) {
        if (score == null) return null;
        return switch (scaleType) {
            case "GLASGOW", "GLASGOW_PEDIATRICO" -> score <= 8 ? RIESGO_MUY_ALTO
                    : (score <= 12 ? RIESGO_ALTO : RIESGO_BAJO);
            case "EVA" -> score <= 3 ? RIESGO_BAJO
                    : (score <= 6 ? RIESGO_MEDIO
                    : (score <= 9 ? RIESGO_ALTO : RIESGO_MUY_ALTO));
            case "NORTON" -> score >= 18 ? RIESGO_BAJO
                    : (score >= 14 ? RIESGO_MEDIO
                    : (score >= 10 ? RIESGO_ALTO : RIESGO_MUY_ALTO));
            case "BRADEN" -> score >= 19 ? RIESGO_BAJO
                    : (score >= 15 ? RIESGO_MEDIO
                    : (score >= 13 ? RIESGO_ALTO : RIESGO_MUY_ALTO));
            case "MORSE" -> score <= 24 ? RIESGO_BAJO
                    : (score <= 44 ? RIESGO_MEDIO : RIESGO_ALTO);
            case "DOWNTON" -> score >= 3 ? RIESGO_ALTO : RIESGO_BAJO;
            case "SILVERMAN" -> score <= 3 ? RIESGO_BAJO
                    : (score <= 6 ? RIESGO_MEDIO : RIESGO_ALTO);
            default -> null;
        };
    }
}

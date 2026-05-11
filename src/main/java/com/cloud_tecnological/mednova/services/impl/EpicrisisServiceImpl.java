package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.epicrisis.CreateEpicrisisRequestDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisFilterParams;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisPreloadDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisResponseDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisTableDto;
import com.cloud_tecnological.mednova.dto.epicrisis.SignEpicrisisRequestDto;
import com.cloud_tecnological.mednova.dto.epicrisis.UpdateEpicrisisRequestDto;
import com.cloud_tecnological.mednova.entity.EpicrisisEntity;
import com.cloud_tecnological.mednova.repositories.epicrisis.EpicrisisJpaRepository;
import com.cloud_tecnological.mednova.repositories.epicrisis.EpicrisisQueryRepository;
import com.cloud_tecnological.mednova.services.EpicrisisService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class EpicrisisServiceImpl implements EpicrisisService {

    private final EpicrisisJpaRepository jpa;
    private final EpicrisisQueryRepository query;

    public EpicrisisServiceImpl(EpicrisisJpaRepository jpa, EpicrisisQueryRepository query) {
        this.jpa = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public EpicrisisResponseDto create(CreateEpicrisisRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        Map<String, Object> adm = query.findAdmisionEgresada(request.getAdmissionId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Admisión no encontrada"));

        // CA1: solo se crea al dar egreso hospitalario.
        LocalDateTime fechaEgresoAdm = toLocalDateTime(adm.get("fecha_egreso"));
        if (fechaEgresoAdm == null) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "La admisión no se encuentra egresada. La epicrisis solo se crea al dar egreso.");
        }

        // Regla: UNIQUE admision_id (una sola epicrisis por admisión).
        if (query.epicrisisExistsForAdmision(request.getAdmissionId(), empresa_id)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Esta admisión ya tiene una epicrisis registrada.");
        }

        validateProfesional(empresa_id, request.getProfessionalId());

        EpicrisisEntity entity = new EpicrisisEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(sede_id);
        entity.setAdmision_id(request.getAdmissionId());
        entity.setPaciente_id(toLong(adm.get("paciente_id")));
        entity.setProfesional_id(request.getProfessionalId());
        entity.setFecha_egreso(request.getDischargeDate() == null ? fechaEgresoAdm : request.getDischargeDate());
        entity.setMotivo_ingreso(request.getAdmissionReason());
        entity.setDiagnostico_ingreso(request.getAdmissionDiagnosis());
        entity.setDiagnostico_egreso(request.getDischargeDiagnosis());
        entity.setProcedimientos_realizados(request.getProceduresPerformed());
        entity.setEvolucion_resumen(request.getEvolutionSummary());
        entity.setComplicaciones(request.getComplications());
        entity.setPlan_seguimiento(request.getFollowUpPlan());
        entity.setMedicamentos_egreso(request.getDischargeMedications());
        entity.setRecomendaciones(request.getRecommendations());
        entity.setIndicaciones_dieta(request.getDietInstructions());
        entity.setIndicaciones_actividad(request.getActivityInstructions());
        entity.setFecha_proximo_control(request.getNextControlDate());
        entity.setPdf_url(request.getPdfUrl());

        if (Boolean.TRUE.equals(request.getSignOnCreate())) {
            entity.setFirmada(true);
            entity.setFecha_firma(LocalDateTime.now());
        } else {
            entity.setFirmada(false);
        }
        entity.setUsuario_creacion(usuario_id);
        EpicrisisEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la epicrisis creada"));
    }

    @Override
    @Transactional
    public EpicrisisResponseDto update(Long id, UpdateEpicrisisRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        EpicrisisEntity entity = getValidEntity(id, empresa_id, sede_id);
        // CA3: una vez firmada, no se edita.
        if (Boolean.TRUE.equals(entity.getFirmada())) {
            throw new GlobalException(HttpStatus.CONFLICT, "Una epicrisis firmada no puede editarse.");
        }
        validateProfesional(empresa_id, request.getProfessionalId());

        entity.setProfesional_id(request.getProfessionalId());
        entity.setMotivo_ingreso(request.getAdmissionReason());
        entity.setDiagnostico_ingreso(request.getAdmissionDiagnosis());
        entity.setDiagnostico_egreso(request.getDischargeDiagnosis());
        entity.setProcedimientos_realizados(request.getProceduresPerformed());
        entity.setEvolucion_resumen(request.getEvolutionSummary());
        entity.setComplicaciones(request.getComplications());
        entity.setPlan_seguimiento(request.getFollowUpPlan());
        entity.setMedicamentos_egreso(request.getDischargeMedications());
        entity.setRecomendaciones(request.getRecommendations());
        entity.setIndicaciones_dieta(request.getDietInstructions());
        entity.setIndicaciones_actividad(request.getActivityInstructions());
        entity.setFecha_proximo_control(request.getNextControlDate());
        if (request.getPdfUrl() != null) entity.setPdf_url(request.getPdfUrl());
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la epicrisis actualizada"));
    }

    @Override
    @Transactional
    public EpicrisisResponseDto sign(Long id, SignEpicrisisRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        EpicrisisEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (Boolean.TRUE.equals(entity.getFirmada())) {
            throw new GlobalException(HttpStatus.CONFLICT, "La epicrisis ya se encuentra firmada.");
        }

        entity.setFirmada(true);
        entity.setFecha_firma(LocalDateTime.now());
        if (request != null && request.getPdfUrl() != null && !request.getPdfUrl().isBlank()) {
            entity.setPdf_url(request.getPdfUrl());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la epicrisis"));
    }

    @Override
    public EpicrisisResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Epicrisis no encontrada"));
    }

    @Override
    public PageImpl<EpicrisisTableDto> list(PageableDto<EpicrisisFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.listEpicrisis(pageable, empresa_id, sede_id);
    }

    @Override
    public EpicrisisPreloadDto preload(Long admissionId) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.preloadFromAdmision(admissionId, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Admisión no encontrada"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        EpicrisisEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (Boolean.TRUE.equals(entity.getFirmada())) {
            throw new GlobalException(HttpStatus.CONFLICT, "Una epicrisis firmada no puede eliminarse.");
        }
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private EpicrisisEntity getValidEntity(Long id, Long empresa_id, Long sede_id) {
        EpicrisisEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Epicrisis no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id())
                || !sede_id.equals(entity.getSede_id())
                || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Epicrisis no encontrada");
        }
        return entity;
    }

    private void validateProfesional(Long empresa_id, Long profesional_id) {
        if (!query.profesionalActivoInEmpresa(profesional_id, empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional no encontrado");
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}

package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.plancuidados.ChangePlanStatusRequestDto;
import com.cloud_tecnological.mednova.dto.plancuidados.CreatePlanCuidadosRequestDto;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosFilterParams;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosResponseDto;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosTableDto;
import com.cloud_tecnological.mednova.dto.plancuidados.UpdatePlanCuidadosRequestDto;
import com.cloud_tecnological.mednova.entity.PlanCuidadosEnfermeriaEntity;
import com.cloud_tecnological.mednova.repositories.plancuidados.PlanCuidadosJpaRepository;
import com.cloud_tecnological.mednova.repositories.plancuidados.PlanCuidadosQueryRepository;
import com.cloud_tecnological.mednova.services.PlanCuidadosService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PlanCuidadosServiceImpl implements PlanCuidadosService {

    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final PlanCuidadosJpaRepository jpa;
    private final PlanCuidadosQueryRepository query;

    public PlanCuidadosServiceImpl(PlanCuidadosJpaRepository jpa,
                                   PlanCuidadosQueryRepository query) {
        this.jpa   = jpa;
        this.query = query;
    }

    @Override
    @Transactional
    public PlanCuidadosResponseDto create(CreatePlanCuidadosRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(empresa_id, request.getPatientId());
        validateAtencion(empresa_id, sede_id, request.getEncounterId());
        validateAtencionMatchesPaciente(request.getEncounterId(), request.getPatientId());
        validateProfesional(empresa_id, request.getProfessionalId());

        PlanCuidadosEnfermeriaEntity entity = new PlanCuidadosEnfermeriaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(sede_id);
        entity.setAtencion_id(request.getEncounterId());
        entity.setPaciente_id(request.getPatientId());
        entity.setProfesional_id(request.getProfessionalId());
        entity.setFecha_plan(request.getPlanDate() == null ? LocalDate.now() : request.getPlanDate());
        entity.setDiagnostico_enfermeria(request.getNursingDiagnosis());
        entity.setObjetivos(request.getObjectives());
        entity.setIntervenciones(request.getInterventions());
        entity.setEvaluacion(request.getEvaluation());
        entity.setEstado(request.getStatus() == null ? ESTADO_ACTIVO : request.getStatus());
        entity.setUsuario_creacion(usuario_id);
        PlanCuidadosEnfermeriaEntity saved = jpa.save(entity);

        return query.findActiveById(saved.getId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el plan de cuidados creado"));
    }

    @Override
    @Transactional
    public PlanCuidadosResponseDto update(Long id, UpdatePlanCuidadosRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        PlanCuidadosEnfermeriaEntity entity = getValidEntity(id, empresa_id, sede_id);

        validateProfesional(empresa_id, request.getProfessionalId());

        entity.setProfesional_id(request.getProfessionalId());
        if (request.getPlanDate() != null) entity.setFecha_plan(request.getPlanDate());
        entity.setDiagnostico_enfermeria(request.getNursingDiagnosis());
        entity.setObjetivos(request.getObjectives());
        entity.setIntervenciones(request.getInterventions());
        entity.setEvaluacion(request.getEvaluation());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entity.setEstado(request.getStatus());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el plan de cuidados actualizado"));
    }

    @Override
    @Transactional
    public PlanCuidadosResponseDto changeStatus(Long id, ChangePlanStatusRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        PlanCuidadosEnfermeriaEntity entity = getValidEntity(id, empresa_id, sede_id);

        entity.setEstado(request.getStatus());
        if (request.getEvaluation() != null && !request.getEvaluation().isBlank()) {
            entity.setEvaluacion(request.getEvaluation());
        }
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el plan de cuidados"));
    }

    @Override
    public PlanCuidadosResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Plan de cuidados no encontrado"));
    }

    @Override
    public PageImpl<PlanCuidadosTableDto> list(PageableDto<PlanCuidadosFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return query.listPlanes(pageable, empresa_id, sede_id);
    }

    @Override
    @Transactional
    public PlanCuidadosResponseDto setActive(Long id, boolean active) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        PlanCuidadosEnfermeriaEntity entity = getValidEntity(id, empresa_id, sede_id);
        entity.setActivo(active);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);

        return query.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el plan de cuidados"));
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        PlanCuidadosEnfermeriaEntity entity = getValidEntity(id, empresa_id, sede_id);
        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpa.save(entity);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private PlanCuidadosEnfermeriaEntity getValidEntity(Long id, Long empresa_id, Long sede_id) {
        PlanCuidadosEnfermeriaEntity entity = jpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Plan de cuidados no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id())
                || !sede_id.equals(entity.getSede_id())
                || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Plan de cuidados no encontrado");
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
}

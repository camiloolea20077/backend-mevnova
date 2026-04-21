package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.profesionalsalud.CreateProfesionalSaludRequestDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.ProfesionalSaludResponseDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.ProfesionalSaludTableDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.UpdateProfesionalSaludRequestDto;
import com.cloud_tecnological.mednova.entity.ProfesionalSaludEntity;
import com.cloud_tecnological.mednova.repositories.profesionalsalud.ProfesionalSaludJpaRepository;
import com.cloud_tecnological.mednova.repositories.profesionalsalud.ProfesionalSaludQueryRepository;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroJpaRepository;
import com.cloud_tecnological.mednova.services.ProfesionalSaludService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfesionalSaludServiceImpl implements ProfesionalSaludService {

    private final ProfesionalSaludJpaRepository   jpaRepository;
    private final ProfesionalSaludQueryRepository queryRepository;
    private final TerceroJpaRepository            terceroJpaRepository;

    public ProfesionalSaludServiceImpl(
            ProfesionalSaludJpaRepository jpaRepository,
            ProfesionalSaludQueryRepository queryRepository,
            TerceroJpaRepository terceroJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.queryRepository      = queryRepository;
        this.terceroJpaRepository = terceroJpaRepository;
    }

    @Override
    @Transactional
    public ProfesionalSaludResponseDto create(CreateProfesionalSaludRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        terceroJpaRepository.findById(request.getThirdPartyId())
                .filter(t -> empresa_id.equals(t.getEmpresa_id()) && t.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado en la empresa"));

        if (queryRepository.existsByTercero(request.getThirdPartyId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El tercero ya está registrado como profesional de salud");
        }

        if (request.getMedicalRegistrationNumber() != null && !request.getMedicalRegistrationNumber().isBlank()
                && queryRepository.existsByRegistroMedico(request.getMedicalRegistrationNumber(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El número de registro médico ya existe en la empresa");
        }

        ProfesionalSaludEntity entity = new ProfesionalSaludEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setTercero_id(request.getThirdPartyId());
        entity.setNumero_registro_medico(request.getMedicalRegistrationNumber());
        entity.setEspecialidad_principal_id(request.getPrimarySpecialtyId());
        entity.setFecha_ingreso(request.getStartDate());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        ProfesionalSaludEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el profesional creado"));
    }

    @Override
    public ProfesionalSaludResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Profesional de salud no encontrado"));
    }

    @Override
    public ProfesionalSaludResponseDto findByThirdParty(Long thirdPartyId) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveByTercero(thirdPartyId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Profesional de salud no encontrado"));
    }

    @Override
    public PageImpl<ProfesionalSaludTableDto> listProfesionales(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listProfesionales(pageable, empresa_id);
    }

    @Override
    @Transactional
    public ProfesionalSaludResponseDto update(Long id, UpdateProfesionalSaludRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ProfesionalSaludEntity entity = getValidEntity(id, empresa_id);

        if (request.getMedicalRegistrationNumber() != null && !request.getMedicalRegistrationNumber().isBlank()
                && queryRepository.existsByRegistroMedico(request.getMedicalRegistrationNumber(), empresa_id, id)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El número de registro médico ya existe en la empresa");
        }

        entity.setNumero_registro_medico(request.getMedicalRegistrationNumber());
        entity.setEspecialidad_principal_id(request.getPrimarySpecialtyId());
        entity.setFecha_ingreso(request.getStartDate());
        entity.setFecha_retiro(request.getRetirementDate());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el profesional actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ProfesionalSaludEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    private ProfesionalSaludEntity getValidEntity(Long id, Long empresa_id) {
        ProfesionalSaludEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Profesional de salud no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional de salud no encontrado");
        }
        return entity;
    }
}

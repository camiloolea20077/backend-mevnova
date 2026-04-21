package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.paciente.CreatePacienteRequestDto;
import com.cloud_tecnological.mednova.dto.paciente.PacienteResponseDto;
import com.cloud_tecnological.mednova.dto.paciente.PacienteTableDto;
import com.cloud_tecnological.mednova.dto.paciente.UpdatePacienteRequestDto;
import com.cloud_tecnological.mednova.entity.PacienteEntity;
import com.cloud_tecnological.mednova.repositories.paciente.PacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.paciente.PacienteQueryRepository;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroJpaRepository;
import com.cloud_tecnological.mednova.services.PacienteService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteServiceImpl implements PacienteService {

    private final PacienteJpaRepository   pacienteJpaRepository;
    private final PacienteQueryRepository pacienteQueryRepository;
    private final TerceroJpaRepository    terceroJpaRepository;

    public PacienteServiceImpl(
            PacienteJpaRepository pacienteJpaRepository,
            PacienteQueryRepository pacienteQueryRepository,
            TerceroJpaRepository terceroJpaRepository) {
        this.pacienteJpaRepository   = pacienteJpaRepository;
        this.pacienteQueryRepository = pacienteQueryRepository;
        this.terceroJpaRepository    = terceroJpaRepository;
    }

    @Override
    @Transactional
    public PacienteResponseDto create(CreatePacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        terceroJpaRepository.findById(request.getThirdPartyId())
                .filter(t -> empresa_id.equals(t.getEmpresa_id()) && t.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado en la empresa"));

        if (pacienteQueryRepository.existsByTercero(request.getThirdPartyId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El tercero ya tiene un paciente registrado");
        }

        PacienteEntity paciente = new PacienteEntity();
        paciente.setEmpresa_id(empresa_id);
        paciente.setTercero_id(request.getThirdPartyId());
        paciente.setGrupo_sanguineo_id(request.getBloodGroupId());
        paciente.setFactor_rh_id(request.getRhFactorId());
        paciente.setDiscapacidad_id(request.getDisabilityId());
        paciente.setGrupo_atencion_id(request.getAttentionGroupId());
        paciente.setAlergias_conocidas(request.getKnownAllergies());
        paciente.setObservaciones_clinicas(request.getClinicalObservations());
        paciente.setUsuario_creacion(usuario_id);

        PacienteEntity saved = pacienteJpaRepository.save(paciente);
        return pacienteQueryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el paciente creado"));
    }

    @Override
    public PacienteResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return pacienteQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    }

    @Override
    public PacienteResponseDto findByThirdParty(Long thirdPartyId) {
        Long empresa_id = TenantContext.getEmpresaId();
        return pacienteQueryRepository.findActiveByTercero(thirdPartyId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
    }

    @Override
    public PageImpl<PacienteTableDto> listPacientes(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return pacienteQueryRepository.listPacientes(pageable, empresa_id);
    }

    @Override
    @Transactional
    public PacienteResponseDto update(Long id, UpdatePacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        PacienteEntity paciente = pacienteJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        if (!empresa_id.equals(paciente.getEmpresa_id()) || paciente.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }

        paciente.setGrupo_sanguineo_id(request.getBloodGroupId());
        paciente.setFactor_rh_id(request.getRhFactorId());
        paciente.setDiscapacidad_id(request.getDisabilityId());
        paciente.setGrupo_atencion_id(request.getAttentionGroupId());
        paciente.setAlergias_conocidas(request.getKnownAllergies());
        paciente.setObservaciones_clinicas(request.getClinicalObservations());
        paciente.setUsuario_modificacion(usuario_id);
        pacienteJpaRepository.save(paciente);

        return pacienteQueryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el paciente actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        PacienteEntity paciente = pacienteJpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));

        if (!empresa_id.equals(paciente.getEmpresa_id()) || paciente.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado");
        }

        paciente.setActivo(!Boolean.TRUE.equals(paciente.getActivo()));
        paciente.setUsuario_modificacion(usuario_id);
        pacienteJpaRepository.save(paciente);
        return paciente.getActivo();
    }
}

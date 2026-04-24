package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.contratopaciente.ContratoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.contratopaciente.CreateContratoPacienteRequestDto;
import com.cloud_tecnological.mednova.entity.ContratoPacienteEntity;
import com.cloud_tecnological.mednova.repositories.contrato.ContratoJpaRepository;
import com.cloud_tecnological.mednova.repositories.contratopaciente.ContratoPacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.contratopaciente.ContratoPacienteQueryRepository;
import com.cloud_tecnological.mednova.repositories.paciente.PacienteJpaRepository;
import com.cloud_tecnological.mednova.services.ContratoPacienteService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContratoPacienteServiceImpl implements ContratoPacienteService {

    private final ContratoPacienteJpaRepository   jpaRepository;
    private final ContratoPacienteQueryRepository queryRepository;
    private final PacienteJpaRepository           pacienteJpaRepository;
    private final ContratoJpaRepository           contratoJpaRepository;

    public ContratoPacienteServiceImpl(
            ContratoPacienteJpaRepository jpaRepository,
            ContratoPacienteQueryRepository queryRepository,
            PacienteJpaRepository pacienteJpaRepository,
            ContratoJpaRepository contratoJpaRepository) {
        this.jpaRepository         = jpaRepository;
        this.queryRepository       = queryRepository;
        this.pacienteJpaRepository  = pacienteJpaRepository;
        this.contratoJpaRepository  = contratoJpaRepository;
    }

    @Override
    @Transactional
    public ContratoPacienteResponseDto create(Long patientId, CreateContratoPacienteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validatePaciente(patientId, empresa_id);

        contratoJpaRepository.findById(request.getContractId())
                .filter(c -> empresa_id.equals(c.getEmpresa_id()) && c.getDeleted_at() == null && Boolean.TRUE.equals(c.getActivo()))
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contrato no encontrado o inactivo en la empresa"));

        ContratoPacienteEntity entity = new ContratoPacienteEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setPaciente_id(patientId);
        entity.setContrato_id(request.getContractId());
        entity.setNumero_poliza(request.getPolicyNumber());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setVigente(request.getCurrent() == null || Boolean.TRUE.equals(request.getCurrent()));
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        ContratoPacienteEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el contrato creado"));
    }

    @Override
    public ContratoPacienteResponseDto findById(Long patientId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contrato del paciente no encontrado"));
    }

    @Override
    public List<ContratoPacienteResponseDto> listByPaciente(Long patientId) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);
        return queryRepository.listByPaciente(patientId, empresa_id);
    }

    @Override
    @Transactional
    public Boolean remove(Long patientId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validatePaciente(patientId, empresa_id);

        ContratoPacienteEntity entity = getValidEntity(id, patientId, empresa_id);
        entity.setActivo(false);
        entity.setUsuario_modificacion(TenantContext.getUsuarioId());
        jpaRepository.save(entity);
        return true;
    }

    private void validatePaciente(Long patientId, Long empresa_id) {
        pacienteJpaRepository.findById(patientId)
                .filter(p -> empresa_id.equals(p.getEmpresa_id()) && p.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Paciente no encontrado en la empresa"));
    }

    private ContratoPacienteEntity getValidEntity(Long id, Long patientId, Long empresa_id) {
        ContratoPacienteEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contrato del paciente no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || !patientId.equals(entity.getPaciente_id()) || !Boolean.TRUE.equals(entity.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Contrato del paciente no encontrado");
        }
        return entity;
    }
}

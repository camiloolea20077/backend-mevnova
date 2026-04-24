package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.serviciocontrato.CreateServicioContratoRequestDto;
import com.cloud_tecnological.mednova.dto.serviciocontrato.ServicioContratoResponseDto;
import com.cloud_tecnological.mednova.dto.serviciocontrato.UpdateServicioContratoRequestDto;
import com.cloud_tecnological.mednova.entity.ServicioContratoEntity;
import com.cloud_tecnological.mednova.repositories.contrato.ContratoJpaRepository;
import com.cloud_tecnological.mednova.repositories.serviciocontrato.ServicioContratoJpaRepository;
import com.cloud_tecnological.mednova.repositories.serviciocontrato.ServicioContratoQueryRepository;
import com.cloud_tecnological.mednova.repositories.serviciosalud.ServicioSaludJpaRepository;
import com.cloud_tecnological.mednova.services.ServicioContratoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicioContratoServiceImpl implements ServicioContratoService {

    private final ServicioContratoJpaRepository    jpaRepository;
    private final ServicioContratoQueryRepository  queryRepository;
    private final ContratoJpaRepository            contratoJpaRepository;
    private final ServicioSaludJpaRepository       servicioJpaRepository;

    public ServicioContratoServiceImpl(
            ServicioContratoJpaRepository jpaRepository,
            ServicioContratoQueryRepository queryRepository,
            ContratoJpaRepository contratoJpaRepository,
            ServicioSaludJpaRepository servicioJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.queryRepository      = queryRepository;
        this.contratoJpaRepository = contratoJpaRepository;
        this.servicioJpaRepository = servicioJpaRepository;
    }

    @Override
    @Transactional
    public ServicioContratoResponseDto create(Long contractId, CreateServicioContratoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validateContrato(contractId, empresa_id);

        servicioJpaRepository.findById(request.getHealthServiceId())
                .filter(s -> empresa_id.equals(s.getEmpresa_id()) && s.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio de salud no encontrado en la empresa"));

        if (queryRepository.existsByServicio(contractId, request.getHealthServiceId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El servicio ya está registrado en este contrato");
        }

        ServicioContratoEntity entity = new ServicioContratoEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setContrato_id(contractId);
        entity.setServicio_salud_id(request.getHealthServiceId());
        entity.setRequiere_autorizacion(request.getRequiresAuthorization() != null ? request.getRequiresAuthorization() : false);
        entity.setCantidad_maxima(request.getMaxQuantity());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        ServicioContratoEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el servicio creado"));
    }

    @Override
    public ServicioContratoResponseDto findById(Long contractId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateContrato(contractId, empresa_id);
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio del contrato no encontrado"));
    }

    @Override
    public List<ServicioContratoResponseDto> listByContrato(Long contractId) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateContrato(contractId, empresa_id);
        return queryRepository.listByContrato(contractId, empresa_id);
    }

    @Override
    @Transactional
    public ServicioContratoResponseDto update(Long contractId, Long id, UpdateServicioContratoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();

        validateContrato(contractId, empresa_id);
        ServicioContratoEntity entity = getValidEntity(id, contractId, empresa_id);

        if (request.getRequiresAuthorization() != null)
            entity.setRequiere_autorizacion(request.getRequiresAuthorization());
        entity.setCantidad_maxima(request.getMaxQuantity());
        entity.setObservaciones(request.getObservations());
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el servicio actualizado"));
    }

    @Override
    @Transactional
    public Boolean remove(Long contractId, Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateContrato(contractId, empresa_id);

        ServicioContratoEntity entity = getValidEntity(id, contractId, empresa_id);
        entity.setActivo(false);
        jpaRepository.save(entity);
        return true;
    }

    private void validateContrato(Long contractId, Long empresa_id) {
        contratoJpaRepository.findById(contractId)
                .filter(c -> empresa_id.equals(c.getEmpresa_id()) && c.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contrato no encontrado en la empresa"));
    }

    private ServicioContratoEntity getValidEntity(Long id, Long contractId, Long empresa_id) {
        ServicioContratoEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio del contrato no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || !contractId.equals(entity.getContrato_id()) || !Boolean.TRUE.equals(entity.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Servicio del contrato no encontrado");
        }
        return entity;
    }
}

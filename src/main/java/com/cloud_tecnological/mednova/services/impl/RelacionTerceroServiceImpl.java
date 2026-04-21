package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.relaciontercero.CreateRelacionTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.RelacionTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.RelacionTerceroTableDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.UpdateRelacionTerceroRequestDto;
import com.cloud_tecnological.mednova.entity.RelacionTerceroEntity;
import com.cloud_tecnological.mednova.repositories.relaciontercero.RelacionTerceroJpaRepository;
import com.cloud_tecnological.mednova.repositories.relaciontercero.RelacionTerceroQueryRepository;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroJpaRepository;
import com.cloud_tecnological.mednova.services.RelacionTerceroService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RelacionTerceroServiceImpl implements RelacionTerceroService {

    private final RelacionTerceroJpaRepository   jpaRepository;
    private final RelacionTerceroQueryRepository queryRepository;
    private final TerceroJpaRepository           terceroJpaRepository;

    public RelacionTerceroServiceImpl(
            RelacionTerceroJpaRepository jpaRepository,
            RelacionTerceroQueryRepository queryRepository,
            TerceroJpaRepository terceroJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.queryRepository      = queryRepository;
        this.terceroJpaRepository = terceroJpaRepository;
    }

    @Override
    @Transactional
    public RelacionTerceroResponseDto create(CreateRelacionTerceroRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (request.getSourceThirdPartyId().equals(request.getDestinationThirdPartyId())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Un tercero no puede relacionarse consigo mismo");
        }

        validateTerceroPertenece(request.getSourceThirdPartyId(), empresa_id);
        validateTerceroPertenece(request.getDestinationThirdPartyId(), empresa_id);

        RelacionTerceroEntity entity = new RelacionTerceroEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setTercero_origen_id(request.getSourceThirdPartyId());
        entity.setTercero_destino_id(request.getDestinationThirdPartyId());
        entity.setTipo_relacion_id(request.getRelationTypeId());
        entity.setEs_responsable(Boolean.TRUE.equals(request.getIsResponsible()));
        entity.setEs_contacto_emergencia(Boolean.TRUE.equals(request.getIsEmergencyContact()));
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        RelacionTerceroEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la relación creada"));
    }

    @Override
    public RelacionTerceroResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Relación no encontrada"));
    }

    @Override
    public List<RelacionTerceroTableDto> listByThirdParty(Long thirdPartyId) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateTerceroPertenece(thirdPartyId, empresa_id);
        return queryRepository.listByThirdParty(thirdPartyId, empresa_id);
    }

    @Override
    @Transactional
    public RelacionTerceroResponseDto update(Long id, UpdateRelacionTerceroRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        RelacionTerceroEntity entity = getValidEntity(id, empresa_id);
        entity.setTipo_relacion_id(request.getRelationTypeId());
        entity.setEs_responsable(Boolean.TRUE.equals(request.getIsResponsible()));
        entity.setEs_contacto_emergencia(Boolean.TRUE.equals(request.getIsEmergencyContact()));
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la relación actualizada"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        RelacionTerceroEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    private void validateTerceroPertenece(Long tercero_id, Long empresa_id) {
        terceroJpaRepository.findById(tercero_id)
                .filter(t -> empresa_id.equals(t.getEmpresa_id()) && t.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado en la empresa"));
    }

    private RelacionTerceroEntity getValidEntity(Long id, Long empresa_id) {
        RelacionTerceroEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Relación no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Relación no encontrada");
        }
        return entity;
    }
}

package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.direcciontercero.CreateDireccionTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.DireccionTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.DireccionTerceroTableDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.UpdateDireccionTerceroRequestDto;
import com.cloud_tecnological.mednova.entity.DireccionTerceroEntity;
import com.cloud_tecnological.mednova.repositories.direcciontercero.DireccionTerceroJpaRepository;
import com.cloud_tecnological.mednova.repositories.direcciontercero.DireccionTerceroQueryRepository;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroJpaRepository;
import com.cloud_tecnological.mednova.services.DireccionTerceroService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DireccionTerceroServiceImpl implements DireccionTerceroService {

    private final DireccionTerceroJpaRepository   jpaRepository;
    private final DireccionTerceroQueryRepository queryRepository;
    private final TerceroJpaRepository            terceroJpaRepository;

    public DireccionTerceroServiceImpl(
            DireccionTerceroJpaRepository jpaRepository,
            DireccionTerceroQueryRepository queryRepository,
            TerceroJpaRepository terceroJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.queryRepository      = queryRepository;
        this.terceroJpaRepository = terceroJpaRepository;
    }

    @Override
    @Transactional
    public DireccionTerceroResponseDto create(CreateDireccionTerceroRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        validateTerceroPertenece(request.getThirdPartyId(), empresa_id);

        if (Boolean.TRUE.equals(request.getIsPrincipal())) {
            queryRepository.unmarkPrincipal(request.getThirdPartyId(), request.getAddressType().toUpperCase(), empresa_id);
        }

        DireccionTerceroEntity entity = new DireccionTerceroEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setTercero_id(request.getThirdPartyId());
        entity.setTipo_direccion(request.getAddressType().toUpperCase());
        entity.setZona_residencia_id(request.getResidenceZoneId());
        entity.setPais_id(request.getCountryId());
        entity.setDepartamento_id(request.getDepartmentId());
        entity.setMunicipio_id(request.getMunicipalityId());
        entity.setDireccion(request.getAddress().trim());
        entity.setBarrio(request.getNeighborhood());
        entity.setCodigo_postal(request.getPostalCode());
        entity.setReferencia(request.getReference());
        entity.setLatitud(request.getLatitude());
        entity.setLongitud(request.getLongitude());
        entity.setEs_principal(Boolean.TRUE.equals(request.getIsPrincipal()));
        entity.setUsuario_creacion(usuario_id);

        DireccionTerceroEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la dirección creada"));
    }

    @Override
    public DireccionTerceroResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));
    }

    @Override
    public List<DireccionTerceroTableDto> listByThirdParty(Long thirdPartyId) {
        Long empresa_id = TenantContext.getEmpresaId();
        validateTerceroPertenece(thirdPartyId, empresa_id);
        return queryRepository.listByThirdParty(thirdPartyId, empresa_id);
    }

    @Override
    @Transactional
    public DireccionTerceroResponseDto update(Long id, UpdateDireccionTerceroRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        DireccionTerceroEntity entity = getValidEntity(id, empresa_id);

        if (Boolean.TRUE.equals(request.getIsPrincipal())) {
            queryRepository.unmarkPrincipal(entity.getTercero_id(), entity.getTipo_direccion(), empresa_id);
        }

        entity.setZona_residencia_id(request.getResidenceZoneId());
        entity.setPais_id(request.getCountryId());
        entity.setDepartamento_id(request.getDepartmentId());
        entity.setMunicipio_id(request.getMunicipalityId());
        entity.setDireccion(request.getAddress().trim());
        entity.setBarrio(request.getNeighborhood());
        entity.setCodigo_postal(request.getPostalCode());
        entity.setReferencia(request.getReference());
        entity.setLatitud(request.getLatitude());
        entity.setLongitud(request.getLongitude());
        entity.setEs_principal(Boolean.TRUE.equals(request.getIsPrincipal()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la dirección actualizada"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        DireccionTerceroEntity entity = getValidEntity(id, empresa_id);
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

    private DireccionTerceroEntity getValidEntity(Long id, Long empresa_id) {
        DireccionTerceroEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Dirección no encontrada");
        }
        return entity;
    }
}

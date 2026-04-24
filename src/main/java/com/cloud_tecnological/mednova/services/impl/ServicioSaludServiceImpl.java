package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.serviciosalud.CreateServicioSaludRequestDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.ServicioSaludResponseDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.ServicioSaludTableDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.UpdateServicioSaludRequestDto;
import com.cloud_tecnological.mednova.entity.ServicioSaludEntity;
import com.cloud_tecnological.mednova.repositories.centrocosto.CentroCostoJpaRepository;
import com.cloud_tecnological.mednova.repositories.serviciosalud.ServicioSaludJpaRepository;
import com.cloud_tecnological.mednova.repositories.serviciosalud.ServicioSaludQueryRepository;
import com.cloud_tecnological.mednova.services.ServicioSaludService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioSaludServiceImpl implements ServicioSaludService {

    private final ServicioSaludJpaRepository   jpaRepository;
    private final ServicioSaludQueryRepository queryRepository;
    private final CentroCostoJpaRepository     centroCostoJpaRepository;

    public ServicioSaludServiceImpl(
            ServicioSaludJpaRepository jpaRepository,
            ServicioSaludQueryRepository queryRepository,
            CentroCostoJpaRepository centroCostoJpaRepository) {
        this.jpaRepository          = jpaRepository;
        this.queryRepository        = queryRepository;
        this.centroCostoJpaRepository = centroCostoJpaRepository;
    }

    @Override
    @Transactional
    public ServicioSaludResponseDto create(CreateServicioSaludRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (queryRepository.existsByInternalCode(request.getInternalCode(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El código interno ya existe en el catálogo de la empresa");
        }

        if (request.getCostCenterId() != null) {
            validateCentroCosto(request.getCostCenterId(), empresa_id);
        }

        ServicioSaludEntity entity = new ServicioSaludEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setCodigo_interno(request.getInternalCode());
        entity.setCodigo_cups(request.getCupsCode());
        entity.setNombre(request.getName());
        entity.setDescripcion(request.getDescription());
        entity.setCategoria_servicio_salud_id(request.getHealthServiceCategoryId());
        entity.setCentro_costo_id(request.getCostCenterId());
        entity.setUnidad_medida(request.getMeasureUnit());
        entity.setRequiere_autorizacion(request.getRequiresAuthorization() != null ? request.getRequiresAuthorization() : false);
        entity.setRequiere_diagnostico(request.getRequiresDiagnosis() != null ? request.getRequiresDiagnosis() : true);
        entity.setUsuario_creacion(usuario_id);

        ServicioSaludEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el servicio creado"));
    }

    @Override
    public ServicioSaludResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio de salud no encontrado"));
    }

    @Override
    @Transactional
    public ServicioSaludResponseDto update(Long id, UpdateServicioSaludRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ServicioSaludEntity entity = getValidEntity(id, empresa_id);

        if (request.getCostCenterId() != null) {
            validateCentroCosto(request.getCostCenterId(), empresa_id);
        }

        entity.setCodigo_cups(request.getCupsCode());
        entity.setNombre(request.getName());
        entity.setDescripcion(request.getDescription());
        entity.setCategoria_servicio_salud_id(request.getHealthServiceCategoryId());
        entity.setCentro_costo_id(request.getCostCenterId());
        entity.setUnidad_medida(request.getMeasureUnit());
        if (request.getRequiresAuthorization() != null) entity.setRequiere_autorizacion(request.getRequiresAuthorization());
        if (request.getRequiresDiagnosis() != null) entity.setRequiere_diagnostico(request.getRequiresDiagnosis());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el servicio actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ServicioSaludEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    @Override
    public PageImpl<ServicioSaludTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listServicios(pageable, empresa_id);
    }

    private ServicioSaludEntity getValidEntity(Long id, Long empresa_id) {
        ServicioSaludEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Servicio de salud no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Servicio de salud no encontrado");
        }
        return entity;
    }

    private void validateCentroCosto(Long centroCostoId, Long empresa_id) {
        centroCostoJpaRepository.findById(centroCostoId)
                .filter(cc -> empresa_id.equals(cc.getEmpresa_id()) && cc.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Centro de costo no encontrado en la empresa"));
    }
}

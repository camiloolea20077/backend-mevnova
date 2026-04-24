package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.centrocosto.CentroCostoResponseDto;
import com.cloud_tecnological.mednova.dto.centrocosto.CentroCostoTableDto;
import com.cloud_tecnological.mednova.dto.centrocosto.CreateCentroCostoRequestDto;
import com.cloud_tecnological.mednova.dto.centrocosto.UpdateCentroCostoRequestDto;
import com.cloud_tecnological.mednova.entity.CentroCostoEntity;
import com.cloud_tecnological.mednova.repositories.centrocosto.CentroCostoJpaRepository;
import com.cloud_tecnological.mednova.repositories.centrocosto.CentroCostoQueryRepository;
import com.cloud_tecnological.mednova.services.CentroCostoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroCostoServiceImpl implements CentroCostoService {

    private final CentroCostoJpaRepository   jpaRepository;
    private final CentroCostoQueryRepository queryRepository;

    public CentroCostoServiceImpl(
            CentroCostoJpaRepository jpaRepository,
            CentroCostoQueryRepository queryRepository) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public CentroCostoResponseDto create(CreateCentroCostoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (queryRepository.existsByCode(request.getCode(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El código de centro de costo ya existe en la empresa");
        }

        if (request.getParentId() != null) {
            validateParent(request.getParentId(), empresa_id);
        }

        CentroCostoEntity entity = new CentroCostoEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setCodigo(request.getCode());
        entity.setNombre(request.getName());
        entity.setCentro_costo_padre_id(request.getParentId());
        entity.setDescripcion(request.getDescription());
        entity.setUsuario_creacion(usuario_id);

        CentroCostoEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el centro de costo creado"));
    }

    @Override
    public CentroCostoResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Centro de costo no encontrado"));
    }

    @Override
    @Transactional
    public CentroCostoResponseDto update(Long id, UpdateCentroCostoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        CentroCostoEntity entity = getValidEntity(id, empresa_id);

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "Un centro de costo no puede ser su propio padre");
            }
            validateParent(request.getParentId(), empresa_id);
        }

        entity.setNombre(request.getName());
        entity.setCentro_costo_padre_id(request.getParentId());
        entity.setDescripcion(request.getDescription());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el centro de costo actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        CentroCostoEntity entity = getValidEntity(id, empresa_id);

        if (Boolean.TRUE.equals(entity.getActivo()) && queryRepository.hasActiveChildren(id, empresa_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "No se puede inactivar un centro de costo con hijos activos");
        }

        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    @Override
    public PageImpl<CentroCostoTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listCentrosCosto(pageable, empresa_id);
    }

    private CentroCostoEntity getValidEntity(Long id, Long empresa_id) {
        CentroCostoEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Centro de costo no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Centro de costo no encontrado");
        }
        return entity;
    }

    private void validateParent(Long parentId, Long empresa_id) {
        CentroCostoEntity parent = jpaRepository.findById(parentId)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Centro de costo padre no encontrado"));
        if (!empresa_id.equals(parent.getEmpresa_id()) || parent.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Centro de costo padre no encontrado");
        }
    }
}

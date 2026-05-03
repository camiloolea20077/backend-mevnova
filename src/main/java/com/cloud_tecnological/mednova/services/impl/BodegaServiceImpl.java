package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.bodega.BodegaResponseDto;
import com.cloud_tecnological.mednova.dto.bodega.BodegaTableDto;
import com.cloud_tecnological.mednova.dto.bodega.CreateBodegaRequestDto;
import com.cloud_tecnological.mednova.dto.bodega.UpdateBodegaRequestDto;
import com.cloud_tecnological.mednova.entity.BodegaEntity;
import com.cloud_tecnological.mednova.repositories.bodega.BodegaJpaRepository;
import com.cloud_tecnological.mednova.repositories.bodega.BodegaQueryRepository;
import com.cloud_tecnological.mednova.services.BodegaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BodegaServiceImpl implements BodegaService {

    private final BodegaJpaRepository   jpaRepository;
    private final BodegaQueryRepository queryRepository;

    public BodegaServiceImpl(BodegaJpaRepository jpaRepository,
                             BodegaQueryRepository queryRepository) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public BodegaResponseDto create(CreateBodegaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (!queryRepository.existsActiveSedeByIdAndEmpresa(request.getBranchId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Sede no encontrada");
        }

        if (request.getResponsibleId() != null
                && !queryRepository.existsProfesionalByIdAndEmpresa(request.getResponsibleId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Responsable no encontrado");
        }

        String codigo = request.getCode().toUpperCase();
        if (queryRepository.existsByCodigoAndSede(codigo, request.getBranchId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe una bodega con ese código en la sede");
        }

        boolean esPrincipal = Boolean.TRUE.equals(request.getIsPrincipal());
        if (esPrincipal) {
            queryRepository.unmarkPrincipalBySede(request.getBranchId(), empresa_id);
        }

        BodegaEntity entity = new BodegaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setSede_id(request.getBranchId());
        entity.setCodigo(codigo);
        entity.setNombre(request.getName());
        entity.setTipo_bodega(request.getWarehouseType());
        entity.setResponsable_id(request.getResponsibleId());
        entity.setUbicacion_fisica(request.getPhysicalLocation());
        entity.setEs_principal(esPrincipal);
        entity.setPermite_dispensar(request.getAllowsDispense() == null ? Boolean.TRUE : request.getAllowsDispense());
        entity.setPermite_recibir(request.getAllowsReceive() == null ? Boolean.TRUE : request.getAllowsReceive());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        BodegaEntity saved = jpaRepository.save(entity);

        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la bodega creada"));
    }

    @Override
    public BodegaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Bodega no encontrada"));
    }

    @Override
    public PageImpl<BodegaTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listBodegas(pageable, empresa_id);
    }

    @Override
    @Transactional
    public BodegaResponseDto update(Long id, UpdateBodegaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        BodegaEntity entity = getValidEntity(id, empresa_id);

        if (request.getResponsibleId() != null
                && !queryRepository.existsProfesionalByIdAndEmpresa(request.getResponsibleId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Responsable no encontrado");
        }

        boolean esPrincipal = Boolean.TRUE.equals(request.getIsPrincipal());
        if (esPrincipal && !Boolean.TRUE.equals(entity.getEs_principal())) {
            queryRepository.unmarkPrincipalBySede(entity.getSede_id(), empresa_id);
        }

        entity.setNombre(request.getName());
        entity.setTipo_bodega(request.getWarehouseType());
        entity.setResponsable_id(request.getResponsibleId());
        entity.setUbicacion_fisica(request.getPhysicalLocation());
        entity.setEs_principal(esPrincipal);
        if (request.getAllowsDispense() != null) entity.setPermite_dispensar(request.getAllowsDispense());
        if (request.getAllowsReceive() != null)  entity.setPermite_recibir(request.getAllowsReceive());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la bodega actualizada"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        BodegaEntity entity = getValidEntity(id, empresa_id);

        // Solo bloqueamos al INACTIVAR: una bodega con stock no puede inactivarse.
        if (Boolean.TRUE.equals(entity.getActivo()) && queryRepository.hasStock(id, empresa_id)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "No se puede inactivar una bodega con stock");
        }

        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private BodegaEntity getValidEntity(Long id, Long empresa_id) {
        BodegaEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Bodega no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Bodega no encontrada");
        }
        return entity;
    }
}

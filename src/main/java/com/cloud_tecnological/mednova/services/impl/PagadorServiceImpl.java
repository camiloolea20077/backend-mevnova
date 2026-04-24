package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.pagador.CreatePagadorRequestDto;
import com.cloud_tecnological.mednova.dto.pagador.PagadorResponseDto;
import com.cloud_tecnological.mednova.dto.pagador.PagadorTableDto;
import com.cloud_tecnological.mednova.dto.pagador.UpdatePagadorRequestDto;
import com.cloud_tecnological.mednova.entity.PagadorEntity;
import com.cloud_tecnological.mednova.repositories.pagador.PagadorJpaRepository;
import com.cloud_tecnological.mednova.repositories.pagador.PagadorQueryRepository;
import com.cloud_tecnological.mednova.repositories.tercero.TerceroJpaRepository;
import com.cloud_tecnological.mednova.services.PagadorService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagadorServiceImpl implements PagadorService {

    private final PagadorJpaRepository   jpaRepository;
    private final PagadorQueryRepository queryRepository;
    private final TerceroJpaRepository   terceroJpaRepository;

    public PagadorServiceImpl(
            PagadorJpaRepository jpaRepository,
            PagadorQueryRepository queryRepository,
            TerceroJpaRepository terceroJpaRepository) {
        this.jpaRepository       = jpaRepository;
        this.queryRepository     = queryRepository;
        this.terceroJpaRepository = terceroJpaRepository;
    }

    @Override
    @Transactional
    public PagadorResponseDto create(CreatePagadorRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        terceroJpaRepository.findById(request.getThirdPartyId())
                .filter(t -> empresa_id.equals(t.getEmpresa_id()) && t.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado en la empresa"));

        if (queryRepository.existsByTercero(request.getThirdPartyId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El tercero ya está registrado como pagador");
        }

        if (queryRepository.existsByCode(request.getCode(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El código de pagador ya existe en la empresa");
        }

        PagadorEntity entity = new PagadorEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setTercero_id(request.getThirdPartyId());
        entity.setCodigo(request.getCode());
        entity.setTipo_pagador_id(request.getPayerTypeId());
        entity.setTipo_cliente_id(request.getClientTypeId());
        entity.setCodigo_eps(request.getEpsCode());
        entity.setCodigo_administradora(request.getAdministratorCode());
        if (request.getFilingDays() != null) entity.setDias_radicacion(request.getFilingDays());
        if (request.getGlossaResponseDays() != null) entity.setDias_respuesta_glosa(request.getGlossaResponseDays());
        entity.setUsuario_creacion(usuario_id);

        PagadorEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el pagador creado"));
    }

    @Override
    public PagadorResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Pagador no encontrado"));
    }

    @Override
    public PagadorResponseDto findByThirdParty(Long thirdPartyId) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveByTercero(thirdPartyId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Pagador no encontrado"));
    }

    @Override
    @Transactional
    public PagadorResponseDto update(Long id, UpdatePagadorRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        PagadorEntity entity = getValidEntity(id, empresa_id);

        entity.setTipo_pagador_id(request.getPayerTypeId());
        entity.setTipo_cliente_id(request.getClientTypeId());
        entity.setCodigo_eps(request.getEpsCode());
        entity.setCodigo_administradora(request.getAdministratorCode());
        if (request.getFilingDays() != null) entity.setDias_radicacion(request.getFilingDays());
        if (request.getGlossaResponseDays() != null) entity.setDias_respuesta_glosa(request.getGlossaResponseDays());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el pagador actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        PagadorEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    @Override
    public PageImpl<PagadorTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listPagadores(pageable, empresa_id);
    }

    private PagadorEntity getValidEntity(Long id, Long empresa_id) {
        PagadorEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Pagador no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Pagador no encontrado");
        }
        return entity;
    }
}

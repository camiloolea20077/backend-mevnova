package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.contrato.*;
import com.cloud_tecnological.mednova.entity.ContratoEntity;
import com.cloud_tecnological.mednova.repositories.contrato.ContratoJpaRepository;
import com.cloud_tecnological.mednova.repositories.contrato.ContratoQueryRepository;
import com.cloud_tecnological.mednova.repositories.pagador.PagadorJpaRepository;
import com.cloud_tecnological.mednova.repositories.tarifario.TarifarioJpaRepository;
import com.cloud_tecnological.mednova.services.ContratoService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContratoServiceImpl implements ContratoService {

    private final ContratoJpaRepository   jpaRepository;
    private final ContratoQueryRepository queryRepository;
    private final PagadorJpaRepository    pagadorJpaRepository;
    private final TarifarioJpaRepository  tarifarioJpaRepository;

    public ContratoServiceImpl(
            ContratoJpaRepository jpaRepository,
            ContratoQueryRepository queryRepository,
            PagadorJpaRepository pagadorJpaRepository,
            TarifarioJpaRepository tarifarioJpaRepository) {
        this.jpaRepository        = jpaRepository;
        this.queryRepository      = queryRepository;
        this.pagadorJpaRepository  = pagadorJpaRepository;
        this.tarifarioJpaRepository = tarifarioJpaRepository;
    }

    @Override
    @Transactional
    public ContratoResponseDto create(CreateContratoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (queryRepository.existsByNumber(request.getNumber(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El número de contrato ya existe en la empresa");
        }

        pagadorJpaRepository.findById(request.getPayerId())
                .filter(p -> empresa_id.equals(p.getEmpresa_id()) && p.getDeleted_at() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Pagador no encontrado en la empresa"));

        if (request.getRateScheduleId() != null) {
            tarifarioJpaRepository.findById(request.getRateScheduleId())
                    .filter(t -> empresa_id.equals(t.getEmpresa_id()) && t.getDeleted_at() == null)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tarifario no encontrado en la empresa"));
        }

        ContratoEntity entity = new ContratoEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setNumero(request.getNumber());
        entity.setPagador_id(request.getPayerId());
        entity.setModalidad_pago_id(request.getPaymentModalityId());
        entity.setTarifario_id(request.getRateScheduleId());
        entity.setObjeto(request.getSubject());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setValor_contrato(request.getContractValue());
        entity.setTecho_mensual(request.getMonthlyLimit());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        ContratoEntity saved = jpaRepository.save(entity);
        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el contrato creado"));
    }

    @Override
    public ContratoResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));
    }

    @Override
    @Transactional
    public ContratoResponseDto update(Long id, UpdateContratoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ContratoEntity entity = getValidEntity(id, empresa_id);

        if (request.getRateScheduleId() != null) {
            tarifarioJpaRepository.findById(request.getRateScheduleId())
                    .filter(t -> empresa_id.equals(t.getEmpresa_id()) && t.getDeleted_at() == null)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Tarifario no encontrado en la empresa"));
        }

        entity.setModalidad_pago_id(request.getPaymentModalityId());
        entity.setTarifario_id(request.getRateScheduleId());
        entity.setObjeto(request.getSubject());
        entity.setFecha_vigencia_desde(request.getValidFrom());
        entity.setFecha_vigencia_hasta(request.getValidUntil());
        entity.setValor_contrato(request.getContractValue());
        entity.setTecho_mensual(request.getMonthlyLimit());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el contrato actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ContratoEntity entity = getValidEntity(id, empresa_id);
        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    @Override
    public PageImpl<ContratoTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listContratos(pageable, empresa_id);
    }

    private ContratoEntity getValidEntity(Long id, Long empresa_id) {
        ContratoEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Contrato no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Contrato no encontrado");
        }
        return entity;
    }
}

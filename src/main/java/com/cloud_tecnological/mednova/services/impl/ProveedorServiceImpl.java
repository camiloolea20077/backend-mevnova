package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.proveedor.CreateProveedorRequestDto;
import com.cloud_tecnological.mednova.dto.proveedor.ProveedorResponseDto;
import com.cloud_tecnological.mednova.dto.proveedor.ProveedorTableDto;
import com.cloud_tecnological.mednova.dto.proveedor.UpdateProveedorRequestDto;
import com.cloud_tecnological.mednova.entity.ProveedorEntity;
import com.cloud_tecnological.mednova.repositories.proveedor.ProveedorJpaRepository;
import com.cloud_tecnological.mednova.repositories.proveedor.ProveedorQueryRepository;
import com.cloud_tecnological.mednova.services.ProveedorService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorJpaRepository   jpaRepository;
    private final ProveedorQueryRepository queryRepository;

    public ProveedorServiceImpl(ProveedorJpaRepository jpaRepository,
                                ProveedorQueryRepository queryRepository) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public ProveedorResponseDto create(CreateProveedorRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (!queryRepository.existsTerceroProveedorByEmpresa(request.getThirdPartyId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Tercero no encontrado o no es de tipo PROVEEDOR");
        }

        if (queryRepository.existsByTercero(request.getThirdPartyId(), empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "El tercero ya está registrado como proveedor");
        }

        String codigo = request.getCode().toUpperCase();
        if (queryRepository.existsByCodigo(codigo, empresa_id, null)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe un proveedor con ese código");
        }

        ProveedorEntity entity = new ProveedorEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setTercero_id(request.getThirdPartyId());
        entity.setCodigo(codigo);
        entity.setCuenta_contable(request.getAccountingAccount());
        entity.setPlazo_pago_dias(request.getPaymentTermDays() == null ? 30 : request.getPaymentTermDays());
        entity.setDescuento_pronto_pago(request.getEarlyPaymentDiscount() == null ? BigDecimal.ZERO : request.getEarlyPaymentDiscount());
        entity.setRequiere_orden_compra(request.getRequiresPurchaseOrder() == null ? Boolean.TRUE : request.getRequiresPurchaseOrder());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_creacion(usuario_id);

        ProveedorEntity saved = jpaRepository.save(entity);

        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el proveedor creado"));
    }

    @Override
    public ProveedorResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
    }

    @Override
    public PageImpl<ProveedorTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listProveedores(pageable, empresa_id);
    }

    @Override
    @Transactional
    public ProveedorResponseDto update(Long id, UpdateProveedorRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ProveedorEntity entity = getValidEntity(id, empresa_id);

        entity.setCuenta_contable(request.getAccountingAccount());
        if (request.getPaymentTermDays() != null) entity.setPlazo_pago_dias(request.getPaymentTermDays());
        if (request.getEarlyPaymentDiscount() != null) entity.setDescuento_pronto_pago(request.getEarlyPaymentDiscount());
        if (request.getRequiresPurchaseOrder() != null) entity.setRequiere_orden_compra(request.getRequiresPurchaseOrder());
        entity.setObservaciones(request.getObservations());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el proveedor actualizado"));
    }

    @Override
    @Transactional
    public Boolean toggleActive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        ProveedorEntity entity = getValidEntity(id, empresa_id);

        entity.setActivo(!Boolean.TRUE.equals(entity.getActivo()));
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return entity.getActivo();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ProveedorEntity getValidEntity(Long id, Long empresa_id) {
        ProveedorEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Proveedor no encontrado");
        }
        return entity;
    }
}

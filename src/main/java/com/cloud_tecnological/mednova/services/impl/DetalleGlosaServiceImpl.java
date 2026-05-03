package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.detalleglosa.CreateDetalleGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.DetalleGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.DetalleGlosaTableDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.GlosaReconciliationDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.UpdateDetalleGlosaRequestDto;
import com.cloud_tecnological.mednova.entity.DetalleGlosaEntity;
import com.cloud_tecnological.mednova.repositories.detalleglosa.DetalleGlosaJpaRepository;
import com.cloud_tecnological.mednova.repositories.detalleglosa.DetalleGlosaQueryRepository;
import com.cloud_tecnological.mednova.services.DetalleGlosaService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DetalleGlosaServiceImpl implements DetalleGlosaService {

    private final DetalleGlosaJpaRepository   jpaRepository;
    private final DetalleGlosaQueryRepository queryRepository;

    public DetalleGlosaServiceImpl(DetalleGlosaJpaRepository jpaRepository,
                                   DetalleGlosaQueryRepository queryRepository) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public DetalleGlosaResponseDto create(CreateDetalleGlosaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        Map<String, Object> glosa = loadOpenGlosa(request.getGlossId(), empresa_id);
        Long facturaId = ((Number) glosa.get("factura_id")).longValue();

        Map<String, Object> detalleFactura = queryRepository
                .findDetalleFacturaSummary(request.getInvoiceItemId(), facturaId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "El ítem de factura no existe o no pertenece a la factura de la glosa"));

        if (!queryRepository.existsActiveMotivoGlosa(request.getGlossReasonId())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Motivo de glosa no encontrado o inactivo");
        }

        BigDecimal totalItem = (BigDecimal) detalleFactura.get("total");
        if (totalItem == null) totalItem = BigDecimal.ZERO;

        BigDecimal yaGlosado = queryRepository.sumGlosadoByDetalleFactura(
                request.getInvoiceItemId(), empresa_id, null);
        BigDecimal acumulado = yaGlosado.add(request.getGlossedValue());
        if (acumulado.compareTo(totalItem) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El valor glosado acumulado del ítem supera el total del mismo en la factura");
        }

        DetalleGlosaEntity entity = new DetalleGlosaEntity();
        entity.setEmpresa_id(empresa_id);
        entity.setGlosa_id(request.getGlossId());
        entity.setDetalle_factura_id(request.getInvoiceItemId());
        entity.setMotivo_glosa_id(request.getGlossReasonId());
        entity.setValor_glosado(request.getGlossedValue());
        entity.setObservacion_pagador(request.getPayerObservation());
        entity.setUsuario_creacion(usuario_id);

        DetalleGlosaEntity saved = jpaRepository.save(entity);

        return queryRepository.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el detalle de glosa creado"));
    }

    @Override
    public DetalleGlosaResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle de glosa no encontrado"));
    }

    @Override
    public List<DetalleGlosaTableDto> listByGlosa(Long glosaId) {
        Long empresa_id = TenantContext.getEmpresaId();
        // Validar que la glosa existe y es de mi empresa antes de listar.
        queryRepository.findGlosaSummary(glosaId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Glosa no encontrada"));
        return queryRepository.listByGlosa(glosaId, empresa_id);
    }

    @Override
    @Transactional
    public DetalleGlosaResponseDto update(Long id, UpdateDetalleGlosaRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        DetalleGlosaEntity entity = getValidEntity(id, empresa_id);

        Map<String, Object> glosa = loadOpenGlosa(entity.getGlosa_id(), empresa_id);
        Long facturaId = ((Number) glosa.get("factura_id")).longValue();

        Map<String, Object> detalleFactura = queryRepository
                .findDetalleFacturaSummary(entity.getDetalle_factura_id(), facturaId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Ítem de factura no encontrado"));

        if (!queryRepository.existsActiveMotivoGlosa(request.getGlossReasonId())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Motivo de glosa no encontrado o inactivo");
        }

        BigDecimal totalItem = (BigDecimal) detalleFactura.get("total");
        if (totalItem == null) totalItem = BigDecimal.ZERO;

        BigDecimal yaGlosado = queryRepository.sumGlosadoByDetalleFactura(
                entity.getDetalle_factura_id(), empresa_id, id);
        BigDecimal acumulado = yaGlosado.add(request.getGlossedValue());
        if (acumulado.compareTo(totalItem) > 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "El valor glosado acumulado del ítem supera el total del mismo en la factura");
        }

        entity.setMotivo_glosa_id(request.getGlossReasonId());
        entity.setValor_glosado(request.getGlossedValue());
        entity.setObservacion_pagador(request.getPayerObservation());
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el detalle de glosa actualizado"));
    }

    @Override
    @Transactional
    public Boolean softDelete(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        DetalleGlosaEntity entity = getValidEntity(id, empresa_id);
        loadOpenGlosa(entity.getGlosa_id(), empresa_id);  // bloquea si no está ABIERTA

        entity.setDeleted_at(LocalDateTime.now());
        entity.setActivo(false);
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);
        return true;
    }

    @Override
    public GlosaReconciliationDto getReconciliation(Long glosaId) {
        Long empresa_id = TenantContext.getEmpresaId();
        Map<String, Object> glosa = queryRepository.findGlosaSummary(glosaId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Glosa no encontrada"));

        BigDecimal valorGlosa = (BigDecimal) glosa.get("valor_total_glosado");
        if (valorGlosa == null) valorGlosa = BigDecimal.ZERO;

        BigDecimal sumDetalles = queryRepository.sumDetallesByGlosa(glosaId, empresa_id);
        Long count = queryRepository.countDetallesByGlosa(glosaId, empresa_id);

        BigDecimal diff = valorGlosa.subtract(sumDetalles);

        return GlosaReconciliationDto.builder()
                .glossId(glosaId)
                .glossTotalValue(valorGlosa)
                .detailsSum(sumDetalles)
                .difference(diff)
                .balanced(diff.compareTo(BigDecimal.ZERO) == 0 && count > 0)
                .detailsCount(count)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> loadOpenGlosa(Long glosaId, Long empresa_id) {
        Map<String, Object> glosa = queryRepository.findGlosaSummary(glosaId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Glosa no encontrada"));
        String estado = (String) glosa.get("estado_glosa");
        if (!"ABIERTA".equals(estado)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Solo se pueden registrar/editar ítems mientras la glosa esté ABIERTA");
        }
        return glosa;
    }

    private DetalleGlosaEntity getValidEntity(Long id, Long empresa_id) {
        DetalleGlosaEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Detalle de glosa no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Detalle de glosa no encontrado");
        }
        return entity;
    }
}

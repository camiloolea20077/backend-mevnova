package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.invoice.*;
import com.cloud_tecnological.mednova.dto.invoiceitem.*;
import com.cloud_tecnological.mednova.entity.DetalleFacturaEntity;
import com.cloud_tecnological.mednova.entity.FacturaEntity;
import com.cloud_tecnological.mednova.repositories.detallefactura.DetalleFacturaJpaRepository;
import com.cloud_tecnological.mednova.repositories.detallefactura.DetalleFacturaQueryRepository;
import com.cloud_tecnological.mednova.repositories.factura.FacturaJpaRepository;
import com.cloud_tecnological.mednova.repositories.factura.FacturaQueryRepository;
import com.cloud_tecnological.mednova.repositories.liquidacion.LiquidacionCobroPacienteQueryRepository;
import com.cloud_tecnological.mednova.services.CuentaPorCobrarService;
import com.cloud_tecnological.mednova.services.InvoiceService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private static final String ESTADO_BORRADOR  = "BORRADOR";
    private static final String ESTADO_APROBADA  = "APROBADA";
    private static final String ESTADO_ANULADA   = "ANULADA";

    private final FacturaJpaRepository facturaJpa;
    private final FacturaQueryRepository facturaQuery;
    private final DetalleFacturaJpaRepository detalleJpa;
    private final DetalleFacturaQueryRepository detalleQuery;
    private final LiquidacionCobroPacienteQueryRepository liquidacionQuery;
    private final CopagoCuotaModeradoraEngine copagoCuota;
    private final CuentaPorCobrarService cuentaPorCobrarService;

    public InvoiceServiceImpl(
            FacturaJpaRepository facturaJpa,
            FacturaQueryRepository facturaQuery,
            DetalleFacturaJpaRepository detalleJpa,
            DetalleFacturaQueryRepository detalleQuery,
            LiquidacionCobroPacienteQueryRepository liquidacionQuery,
            CopagoCuotaModeradoraEngine copagoCuota,
            CuentaPorCobrarService cuentaPorCobrarService) {
        this.facturaJpa             = facturaJpa;
        this.facturaQuery           = facturaQuery;
        this.detalleJpa             = detalleJpa;
        this.detalleQuery           = detalleQuery;
        this.liquidacionQuery       = liquidacionQuery;
        this.copagoCuota            = copagoCuota;
        this.cuentaPorCobrarService = cuentaPorCobrarService;
    }

    @Override
    @Transactional
    public InvoiceResponseDto create(CreateInvoiceRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        if (facturaQuery.existsFacturaForAdmision(dto.getAdmissionId(), empresaId)) {
            throw new GlobalException(HttpStatus.CONFLICT, "Ya existe una factura para esta admisión");
        }

        Long estadoBorradorId = facturaQuery.findEstadoFacturaIdByCodigo(ESTADO_BORRADOR);
        if (estadoBorradorId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado de factura 'BORRADOR' no configurado en el sistema");
        }

        String numero = facturaQuery.generateNumeroFactura(empresaId, dto.getPrefix());

        FacturaEntity factura = new FacturaEntity();
        factura.setEmpresa_id(empresaId);
        factura.setSede_id(sedeId);
        factura.setPrefijo(dto.getPrefix());
        factura.setNumero(numero);
        factura.setAdmision_id(dto.getAdmissionId());
        factura.setEstado_factura_id(estadoBorradorId);
        factura.setObservaciones(dto.getObservations());
        factura.setUsuario_creacion(usuarioId);

        // Precargar paciente, pagador y contrato desde la admisión
        Map<String, Object> admCtx = facturaQuery.findAdmisionContext(dto.getAdmissionId(), empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST, "Admisión no encontrada o no pertenece a la empresa"));
        factura.setPaciente_id(toLong(admCtx.get("paciente_id")));
        factura.setPagador_id(toLong(admCtx.get("pagador_id")));
        factura.setContrato_id(toLong(admCtx.get("contrato_id")));

        FacturaEntity saved = facturaJpa.save(factura);

        // Precargar servicios de órdenes clínicas de la admisión
        if (factura.getContrato_id() != null) {
            preloadItemsFromAdmision(saved, dto.getAdmissionId(), factura.getContrato_id(), empresaId, usuarioId);
        }

        recalculateTotals(saved, empresaId);

        return facturaQuery.findActiveById(saved.getId(), empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la factura creada"));
    }

    @Override
    public InvoiceResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return facturaQuery.findActiveById(id, empresaId, sedeId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));
    }

    @Override
    public PageImpl<InvoiceTableDto> listActive(PageableDto<?> request) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        return facturaQuery.listActive(request, empresaId, sedeId);
    }

    @Override
    @Transactional
    public Boolean approve(Long id, ApproveInvoiceRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        FacturaEntity entity = facturaJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String estadoCodigo = facturaQuery.findEstadoFacturaCodigoById(entity.getEstado_factura_id());
        if (!ESTADO_BORRADOR.equals(estadoCodigo)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Solo se pueden aprobar facturas en estado BORRADOR");
        }

        Long estadoAprobadaId = facturaQuery.findEstadoFacturaIdByCodigo(ESTADO_APROBADA);
        if (estadoAprobadaId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'APROBADA' no configurado en el sistema");
        }

        // Calcular copago/cuota moderadora por ítem (HU-056)
        Map<String, Object> billingCtx = liquidacionQuery.findPatientBillingContext(entity.getPaciente_id(), empresaId);
        Long regimenId = toLong(billingCtx.get("regimen_id"));
        Long categoriaSisbenId = toLong(billingCtx.get("categoria_sisben_id"));

        if (regimenId != null) {
            List<DetalleFacturaEntity> items = detalleJpa.findActiveByFacturaIdAndEmpresaId(id, empresaId);
            for (DetalleFacturaEntity item : items) {
                BigDecimal base = item.getSubtotal() != null ? item.getSubtotal() : BigDecimal.ZERO;

                BigDecimal valorCopago = copagoCuota.calcular(
                    empresaId, entity.getPaciente_id(), entity.getAdmision_id(),
                    item.getAtencion_id(), id, item.getServicio_salud_id(),
                    "COPAGO", base, regimenId, categoriaSisbenId, usuarioId);

                BigDecimal valorCuota = copagoCuota.calcular(
                    empresaId, entity.getPaciente_id(), entity.getAdmision_id(),
                    item.getAtencion_id(), id, item.getServicio_salud_id(),
                    "CUOTA_MODERADORA", base, regimenId, categoriaSisbenId, usuarioId);

                item.setValor_copago(valorCopago);
                item.setValor_cuota_moderadora(valorCuota);
                BigDecimal iva      = item.getValor_iva() != null ? item.getValor_iva() : BigDecimal.ZERO;
                BigDecimal descuento = item.getValor_descuento() != null ? item.getValor_descuento() : BigDecimal.ZERO;
                item.setTotal(base.add(iva).subtract(descuento).add(valorCopago).add(valorCuota));
                detalleJpa.save(item);
            }
            recalculateTotals(entity, empresaId);
        }

        entity.setEstado_factura_id(estadoAprobadaId);
        if (dto.getObservations() != null && !dto.getObservations().isBlank()) {
            entity.setObservaciones(dto.getObservations());
        }
        entity.setUsuario_modificacion(usuarioId);
        facturaJpa.save(entity);

        // HU-059: Crear cuenta por cobrar automáticamente
        cuentaPorCobrarService.crearDesdeFactura(entity, usuarioId);

        return true;
    }

    @Override
    @Transactional
    public Boolean cancel(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        FacturaEntity entity = facturaJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        validateTenantAndNotDeleted(entity, empresaId, sedeId);

        String estadoCodigo = facturaQuery.findEstadoFacturaCodigoById(entity.getEstado_factura_id());
        if (ESTADO_ANULADA.equals(estadoCodigo)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La factura ya está anulada");
        }
        if (ESTADO_APROBADA.equals(estadoCodigo)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "No se puede anular una factura aprobada");
        }

        Long estadoAnuladaId = facturaQuery.findEstadoFacturaIdByCodigo(ESTADO_ANULADA);
        if (estadoAnuladaId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado 'ANULADA' no configurado en el sistema");
        }

        entity.setEstado_factura_id(estadoAnuladaId);
        entity.setUsuario_modificacion(usuarioId);
        facturaJpa.save(entity);
        return true;
    }

    // ---- Items (HU-055) ----

    @Override
    @Transactional
    public InvoiceItemResponseDto addItem(Long facturaId, AddInvoiceItemRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        FacturaEntity factura = facturaJpa.findById(facturaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        validateTenantAndNotDeleted(factura, empresaId, sedeId);
        validateDraft(factura);

        DetalleFacturaEntity item = buildItem(factura, dto.getHealthServiceId(), dto.getAttentionId(),
            dto.getQuantity(), dto.getUnitValue(),
            dto.getIvaPercentage() != null ? dto.getIvaPercentage() : BigDecimal.ZERO,
            dto.getDiscountValue() != null ? dto.getDiscountValue() : BigDecimal.ZERO,
            dto.getDiagnosisId(), dto.getObservations(), empresaId);

        DetalleFacturaEntity saved = detalleJpa.save(item);
        recalculateTotals(factura, empresaId);

        return detalleQuery.findActiveById(saved.getId(), empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el ítem"));
    }

    @Override
    @Transactional
    public InvoiceItemResponseDto updateItem(Long facturaId, Long itemId, UpdateInvoiceItemRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();
        Long usuarioId = TenantContext.getUsuarioId();

        FacturaEntity factura = facturaJpa.findById(facturaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        validateTenantAndNotDeleted(factura, empresaId, sedeId);
        validateDraft(factura);

        DetalleFacturaEntity item = detalleJpa.findById(itemId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Ítem no encontrado"));

        if (!item.getFactura_id().equals(facturaId) || !item.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Ítem no encontrado");
        }
        if (!Boolean.TRUE.equals(item.getActivo())) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Ítem no encontrado");
        }

        BigDecimal ivaPorc = dto.getIvaPercentage() != null ? dto.getIvaPercentage() : BigDecimal.ZERO;
        BigDecimal descuento = dto.getDiscountValue() != null ? dto.getDiscountValue() : BigDecimal.ZERO;

        BigDecimal subtotal  = dto.getQuantity().multiply(dto.getUnitValue());
        BigDecimal valorIva  = subtotal.multiply(ivaPorc).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal total     = subtotal.add(valorIva).subtract(descuento)
                                .add(item.getValor_copago() != null ? item.getValor_copago() : BigDecimal.ZERO)
                                .add(item.getValor_cuota_moderadora() != null ? item.getValor_cuota_moderadora() : BigDecimal.ZERO);

        item.setCantidad(dto.getQuantity());
        item.setValor_unitario(dto.getUnitValue());
        item.setPorcentaje_iva(ivaPorc);
        item.setValor_iva(valorIva);
        item.setValor_descuento(descuento);
        item.setSubtotal(subtotal);
        item.setTotal(total);
        if (dto.getDiagnosisId() != null) item.setDiagnostico_id(dto.getDiagnosisId());
        if (dto.getObservations() != null) item.setObservaciones(dto.getObservations());

        detalleJpa.save(item);
        recalculateTotals(factura, empresaId);

        return detalleQuery.findActiveById(itemId, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar el ítem"));
    }

    @Override
    @Transactional
    public Boolean deleteItem(Long facturaId, Long itemId) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();

        FacturaEntity factura = facturaJpa.findById(facturaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        validateTenantAndNotDeleted(factura, empresaId, sedeId);
        validateDraft(factura);

        DetalleFacturaEntity item = detalleJpa.findById(itemId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Ítem no encontrado"));

        if (!item.getFactura_id().equals(facturaId) || !item.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Ítem no encontrado");
        }

        item.setActivo(false);
        detalleJpa.save(item);
        recalculateTotals(factura, empresaId);
        return true;
    }

    @Override
    public PageImpl<InvoiceItemTableDto> listItems(Long facturaId, PageableDto<?> request) {
        Long empresaId = TenantContext.getEmpresaId();
        Long sedeId    = TenantContext.getSedeId();

        FacturaEntity factura = facturaJpa.findById(facturaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        validateTenantAndNotDeleted(factura, empresaId, sedeId);
        return detalleQuery.listByFactura(facturaId, empresaId, request);
    }

    // ---- helpers ----

    private void preloadItemsFromAdmision(FacturaEntity factura, Long admisionId, Long contratoId,
                                           Long empresaId, Long usuarioId) {
        List<Map<String, Object>> servicios = facturaQuery.preloadServicesFromAdmision(
            admisionId, contratoId, empresaId);

        for (Map<String, Object> svc : servicios) {
            Long servicioId   = toLong(svc.get("servicio_salud_id"));
            BigDecimal cant   = toBigDecimal(svc.get("cantidad"));
            BigDecimal valor  = toBigDecimal(svc.get("valor_unitario"));
            Long atencionId   = svc.get("atencion_id") != null ? toLong(svc.get("atencion_id")) : null;

            DetalleFacturaEntity item = buildItem(factura, servicioId, atencionId,
                cant, valor, BigDecimal.ZERO, BigDecimal.ZERO, null, null, empresaId);
            detalleJpa.save(item);
        }
    }

    private DetalleFacturaEntity buildItem(FacturaEntity factura, Long servicioId, Long atencionId,
            BigDecimal cantidad, BigDecimal valorUnitario, BigDecimal ivaPorc, BigDecimal descuento,
            Long diagnosticoId, String observaciones, Long empresaId) {

        BigDecimal subtotal = cantidad.multiply(valorUnitario);
        BigDecimal valorIva = subtotal.multiply(ivaPorc)
            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal total    = subtotal.add(valorIva).subtract(descuento);

        DetalleFacturaEntity item = new DetalleFacturaEntity();
        item.setEmpresa_id(empresaId);
        item.setFactura_id(factura.getId());
        item.setServicio_salud_id(servicioId);
        item.setAtencion_id(atencionId);
        item.setCantidad(cantidad);
        item.setValor_unitario(valorUnitario);
        item.setPorcentaje_iva(ivaPorc);
        item.setValor_iva(valorIva);
        item.setValor_descuento(descuento);
        item.setValor_copago(BigDecimal.ZERO);
        item.setValor_cuota_moderadora(BigDecimal.ZERO);
        item.setSubtotal(subtotal);
        item.setTotal(total);
        item.setDiagnostico_id(diagnosticoId);
        item.setObservaciones(observaciones);
        return item;
    }

    private void recalculateTotals(FacturaEntity factura, Long empresaId) {
        BigDecimal[] totals = detalleQuery.calculateTotals(factura.getId(), empresaId);
        factura.setSubtotal(totals[0]);
        factura.setTotal_iva(totals[1]);
        factura.setTotal_descuento(totals[2]);
        factura.setTotal_copago(totals[3]);
        factura.setTotal_cuota_moderadora(totals[4]);
        factura.setTotal_neto(totals[5]);
        facturaJpa.save(factura);
    }

    private void validateDraft(FacturaEntity factura) {
        String codigo = facturaQuery.findEstadoFacturaCodigoById(factura.getEstado_factura_id());
        if (!ESTADO_BORRADOR.equals(codigo)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "Solo se pueden modificar facturas en estado BORRADOR");
        }
    }

    private void validateTenantAndNotDeleted(FacturaEntity entity, Long empresaId, Long sedeId) {
        if (entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada");
        }
        if (!entity.getEmpresa_id().equals(empresaId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada");
        }
        if (!entity.getSede_id().equals(sedeId)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada");
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        return ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        return BigDecimal.valueOf(((Number) value).doubleValue());
    }
}

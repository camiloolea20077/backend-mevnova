package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.compra.CancelCompraRequestDto;
import com.cloud_tecnological.mednova.dto.compra.CompraResponseDto;
import com.cloud_tecnological.mednova.dto.compra.CompraTableDto;
import com.cloud_tecnological.mednova.dto.compra.CreateCompraRequestDto;
import com.cloud_tecnological.mednova.dto.compra.CreateDetalleCompraRequestDto;
import com.cloud_tecnological.mednova.entity.CompraEntity;
import com.cloud_tecnological.mednova.entity.DetalleCompraEntity;
import com.cloud_tecnological.mednova.entity.LoteEntity;
import com.cloud_tecnological.mednova.entity.MovimientoInventarioEntity;
import com.cloud_tecnological.mednova.entity.StockLoteEntity;
import com.cloud_tecnological.mednova.repositories.compra.CompraJpaRepository;
import com.cloud_tecnological.mednova.repositories.compra.CompraQueryRepository;
import com.cloud_tecnological.mednova.repositories.compra.DetalleCompraJpaRepository;
import com.cloud_tecnological.mednova.repositories.lote.LoteJpaRepository;
import com.cloud_tecnological.mednova.repositories.movimiento.MovimientoInventarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.stock.StockLoteJpaRepository;
import com.cloud_tecnological.mednova.services.CompraService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraServiceImpl implements CompraService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final String ESTADO_BORRADOR = "BORRADOR";
    private static final String ESTADO_RECIBIDA = "RECIBIDA";
    private static final String ESTADO_ANULADA  = "ANULADA";
    private static final String ESTADO_PAGADA   = "PAGADA";

    private final CompraJpaRepository                compraJpa;
    private final CompraQueryRepository              compraQuery;
    private final DetalleCompraJpaRepository         detalleJpa;
    private final LoteJpaRepository                  loteJpa;
    private final StockLoteJpaRepository             stockJpa;
    private final MovimientoInventarioJpaRepository  movimientoJpa;

    public CompraServiceImpl(CompraJpaRepository compraJpa,
                             CompraQueryRepository compraQuery,
                             DetalleCompraJpaRepository detalleJpa,
                             LoteJpaRepository loteJpa,
                             StockLoteJpaRepository stockJpa,
                             MovimientoInventarioJpaRepository movimientoJpa) {
        this.compraJpa     = compraJpa;
        this.compraQuery   = compraQuery;
        this.detalleJpa    = detalleJpa;
        this.loteJpa       = loteJpa;
        this.stockJpa      = stockJpa;
        this.movimientoJpa = movimientoJpa;
    }

    @Override
    @Transactional
    public CompraResponseDto create(CreateCompraRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (!compraQuery.existsBodegaActivaPermiteRecibir(request.getWarehouseId(), empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Bodega no encontrada o no permite recibir mercancía");
        }
        if (!compraQuery.existsProveedorActivo(request.getSupplierId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Proveedor no encontrado o inactivo");
        }
        if (request.getReceptionDate() != null && request.getReceptionDate().isBefore(request.getPurchaseDate())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La fecha de recepción no puede ser anterior a la fecha de compra");
        }

        LocalDate today = LocalDate.now();
        for (CreateDetalleCompraRequestDto item : request.getItems()) {
            if (!item.getExpirationDate().isAfter(today)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "La fecha de vencimiento debe ser futura");
            }
            if (item.getManufacturingDate() != null && item.getManufacturingDate().isAfter(item.getExpirationDate())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST, "La fecha de fabricación no puede ser posterior al vencimiento");
            }
            if (!compraQuery.isServicioMedicamentoOInsumo(item.getHealthServiceId(), empresa_id)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "El servicio " + item.getHealthServiceId() + " no es de categoría MEDICAMENTO o INSUMO");
            }
        }

        CompraEntity compra = new CompraEntity();
        compra.setEmpresa_id(empresa_id);
        compra.setSede_id(sede_id);
        compra.setBodega_id(request.getWarehouseId());
        compra.setProveedor_id(request.getSupplierId());
        compra.setNumero_compra(compraQuery.generateNextNumeroCompra(empresa_id));
        compra.setNumero_factura_proveedor(request.getSupplierInvoiceNumber());
        compra.setFecha_compra(request.getPurchaseDate());
        compra.setFecha_recepcion(request.getReceptionDate());
        compra.setEstado_compra(ESTADO_BORRADOR);
        compra.setSoporte_url(request.getSupportUrl());
        compra.setObservaciones(request.getObservations());
        compra.setUsuario_creacion(usuario_id);

        BigDecimal subtotal       = BigDecimal.ZERO;
        BigDecimal totalIva       = BigDecimal.ZERO;
        BigDecimal totalDescuento = BigDecimal.ZERO;
        BigDecimal total          = BigDecimal.ZERO;

        CompraEntity savedCompra = compraJpa.save(compra);

        for (CreateDetalleCompraRequestDto item : request.getItems()) {
            Long loteId = resolveOrCreateLote(item, request.getSupplierId(), empresa_id, usuario_id);

            BigDecimal lineSubtotal       = item.getQuantity().multiply(item.getUnitValue()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pctIva             = item.getVatPercentage()      != null ? item.getVatPercentage()      : BigDecimal.ZERO;
            BigDecimal pctDescuento       = item.getDiscountPercentage() != null ? item.getDiscountPercentage() : BigDecimal.ZERO;
            BigDecimal lineDescuento      = lineSubtotal.multiply(pctDescuento).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            BigDecimal baseGravable       = lineSubtotal.subtract(lineDescuento);
            BigDecimal lineIva            = baseGravable.multiply(pctIva).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            BigDecimal lineTotal          = baseGravable.add(lineIva);

            DetalleCompraEntity detalle = new DetalleCompraEntity();
            detalle.setEmpresa_id(empresa_id);
            detalle.setCompra_id(savedCompra.getId());
            detalle.setServicio_salud_id(item.getHealthServiceId());
            detalle.setLote_id(loteId);
            detalle.setCantidad(item.getQuantity());
            detalle.setValor_unitario(item.getUnitValue());
            detalle.setPorcentaje_iva(pctIva);
            detalle.setValor_iva(lineIva);
            detalle.setPorcentaje_descuento(pctDescuento);
            detalle.setValor_descuento(lineDescuento);
            detalle.setSubtotal(lineSubtotal);
            detalle.setTotal(lineTotal);
            detalle.setObservaciones(item.getObservations());
            detalleJpa.save(detalle);

            subtotal       = subtotal.add(lineSubtotal);
            totalDescuento = totalDescuento.add(lineDescuento);
            totalIva       = totalIva.add(lineIva);
            total          = total.add(lineTotal);
        }

        savedCompra.setSubtotal(subtotal);
        savedCompra.setTotal_iva(totalIva);
        savedCompra.setTotal_descuento(totalDescuento);
        savedCompra.setTotal(total);
        compraJpa.save(savedCompra);

        return compraQuery.findActiveById(savedCompra.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la compra creada"));
    }

    @Override
    public CompraResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return compraQuery.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
    }

    @Override
    public PageImpl<CompraTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return compraQuery.listCompras(pageable, empresa_id);
    }

    @Override
    @Transactional
    public CompraResponseDto receive(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        CompraEntity compra = getValidEntity(id, empresa_id);
        if (!ESTADO_BORRADOR.equals(compra.getEstado_compra())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo las compras en estado BORRADOR pueden ser recibidas");
        }

        List<DetalleCompraEntity> detalles = findDetallesByCompra(compra.getId(), empresa_id);
        if (detalles.isEmpty()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "La compra no tiene ítems registrados");
        }

        LocalDateTime now = LocalDateTime.now();
        for (DetalleCompraEntity detalle : detalles) {
            applyStockEntryAndMovement(detalle, compra, sede_id, empresa_id, usuario_id, now);
        }

        compra.setEstado_compra(ESTADO_RECIBIDA);
        if (compra.getFecha_recepcion() == null) {
            compra.setFecha_recepcion(LocalDate.now());
        }
        compra.setUsuario_modificacion(usuario_id);
        compraJpa.save(compra);

        return compraQuery.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la compra"));
    }

    @Override
    @Transactional
    public CompraResponseDto cancel(Long id, CancelCompraRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        CompraEntity compra = getValidEntity(id, empresa_id);
        if (ESTADO_ANULADA.equals(compra.getEstado_compra())) {
            throw new GlobalException(HttpStatus.CONFLICT, "La compra ya está anulada");
        }
        if (ESTADO_PAGADA.equals(compra.getEstado_compra())) {
            throw new GlobalException(HttpStatus.CONFLICT, "Una compra pagada no puede anularse");
        }
        if (ESTADO_RECIBIDA.equals(compra.getEstado_compra())) {
            // Una compra ya recibida no admite ediciones; solo anulación con justificación,
            // y el ajuste de stock debe hacerse por flujo de ajuste de inventario (HU-077).
            // Por eso aquí no revertimos stock automáticamente.
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Una compra recibida solo puede revertirse mediante un ajuste de inventario");
        }

        String motivo = request.getReason();
        String observaciones = compra.getObservaciones() == null ? "" : compra.getObservaciones() + "\n";
        compra.setObservaciones(observaciones + "[ANULACIÓN] " + motivo);
        compra.setEstado_compra(ESTADO_ANULADA);
        compra.setUsuario_modificacion(usuario_id);
        compraJpa.save(compra);

        return compraQuery.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la compra"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private CompraEntity getValidEntity(Long id, Long empresa_id) {
        CompraEntity entity = compraJpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Compra no encontrada");
        }
        return entity;
    }

    private List<DetalleCompraEntity> findDetallesByCompra(Long compra_id, Long empresa_id) {
        // Reusamos el listado de detalles desde QueryRepository pero necesitamos las entidades para mutar.
        // Hacemos una consulta simple por id derivada de los DTOs.
        return compraQuery.findItemsByCompra(compra_id, empresa_id).stream()
                .map(d -> detalleJpa.findById(d.getId())
                        .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Detalle de compra inconsistente")))
                .toList();
    }

    private Long resolveOrCreateLote(CreateDetalleCompraRequestDto item, Long proveedor_id,
                                     Long empresa_id, Long usuario_id) {
        return compraQuery.findLoteIdByEmpresaServicioNumero(empresa_id, item.getHealthServiceId(), item.getBatchNumber())
                .orElseGet(() -> {
                    LoteEntity lote = new LoteEntity();
                    lote.setEmpresa_id(empresa_id);
                    lote.setServicio_salud_id(item.getHealthServiceId());
                    lote.setNumero_lote(item.getBatchNumber());
                    lote.setFecha_fabricacion(item.getManufacturingDate());
                    lote.setFecha_vencimiento(item.getExpirationDate());
                    lote.setRegistro_invima(item.getInvimaRegister());
                    lote.setProveedor_id(proveedor_id);
                    lote.setUsuario_creacion(usuario_id);
                    return loteJpa.save(lote).getId();
                });
    }

    private void applyStockEntryAndMovement(DetalleCompraEntity detalle, CompraEntity compra,
                                            Long sede_id, Long empresa_id, Long usuario_id,
                                            LocalDateTime now) {
        Long stockId = compraQuery.findStockLoteIdByBodegaLote(compra.getBodega_id(), detalle.getLote_id(), empresa_id)
                .orElse(null);

        StockLoteEntity stock;
        if (stockId == null) {
            stock = new StockLoteEntity();
            stock.setEmpresa_id(empresa_id);
            stock.setSede_id(sede_id);
            stock.setBodega_id(compra.getBodega_id());
            stock.setLote_id(detalle.getLote_id());
            stock.setCantidad_disponible(detalle.getCantidad());
            stock.setCantidad_reservada(BigDecimal.ZERO);
        } else {
            stock = stockJpa.findById(stockId)
                    .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Stock inconsistente"));
            stock.setCantidad_disponible(stock.getCantidad_disponible().add(detalle.getCantidad()));
        }
        stock.setUltimo_movimiento_at(now);
        stockJpa.save(stock);

        MovimientoInventarioEntity movimiento = new MovimientoInventarioEntity();
        movimiento.setEmpresa_id(empresa_id);
        movimiento.setSede_id(sede_id);
        movimiento.setTipo_movimiento("ENTRADA_COMPRA");
        movimiento.setBodega_destino_id(compra.getBodega_id());
        movimiento.setLote_id(detalle.getLote_id());
        movimiento.setServicio_salud_id(detalle.getServicio_salud_id());
        movimiento.setCantidad(detalle.getCantidad());
        movimiento.setValor_unitario(detalle.getValor_unitario());
        movimiento.setReferencia_tipo("COMPRA");
        movimiento.setReferencia_id(compra.getId());
        movimiento.setMotivo("Recepción compra " + compra.getNumero_compra());
        movimiento.setFecha_movimiento(now);
        movimiento.setUsuario_creacion(usuario_id);
        movimientoJpa.save(movimiento);
    }
}

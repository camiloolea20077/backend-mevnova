package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.devolucion.CreateDevolucionRequestDto;
import com.cloud_tecnological.mednova.dto.devolucion.DevolucionItemResponseDto;
import com.cloud_tecnological.mednova.dto.devolucion.ReturnItemRequestDto;
import com.cloud_tecnological.mednova.entity.MovimientoInventarioEntity;
import com.cloud_tecnological.mednova.entity.StockLoteEntity;
import com.cloud_tecnological.mednova.repositories.devolucion.DevolucionQueryRepository;
import com.cloud_tecnological.mednova.repositories.movimiento.MovimientoInventarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.stock.StockLoteJpaRepository;
import com.cloud_tecnological.mednova.services.DevolucionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DevolucionServiceImpl implements DevolucionService {

    private static final String ESTADO_ANULADA               = "ANULADA";
    private static final String TIPO_DEVOLUCION_PACIENTE     = "DEVOLUCION_PACIENTE";
    private static final String TIPO_BAJA_VENCIMIENTO        = "BAJA_VENCIMIENTO";
    private static final String REFERENCIA_TIPO              = "DEVOLUCION_DISPENSACION";

    private final DevolucionQueryRepository       queryRepository;
    private final StockLoteJpaRepository          stockJpa;
    private final MovimientoInventarioJpaRepository movimientoJpa;

    public DevolucionServiceImpl(DevolucionQueryRepository queryRepository,
                                 StockLoteJpaRepository stockJpa,
                                 MovimientoInventarioJpaRepository movimientoJpa) {
        this.queryRepository = queryRepository;
        this.stockJpa        = stockJpa;
        this.movimientoJpa   = movimientoJpa;
    }

    @Override
    @Transactional
    public List<DevolucionItemResponseDto> registerReturn(Long dispensacionId, CreateDevolucionRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        Map<String, Object> dispensacion = queryRepository
                .findDispensacion(dispensacionId, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Dispensación no encontrada"));

        if (ESTADO_ANULADA.equals(dispensacion.get("estado"))) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "No se puede registrar devolución sobre una dispensación anulada");
        }

        if (!queryRepository.existsBodegaActivaPermiteRecibir(request.getTargetWarehouseId(), empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND,
                    "Bodega de destino no encontrada o no permite recibir");
        }

        String bodegaNombre = queryRepository
                .findBodegaNombre(request.getTargetWarehouseId(), empresa_id)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        LocalDate today  = LocalDate.now();
        List<DevolucionItemResponseDto> response = new ArrayList<>();

        for (ReturnItemRequestDto item : request.getItems()) {
            Map<String, Object> detalle = queryRepository
                    .findDetalleDispensacion(item.getDispensationDetailId(), dispensacionId, empresa_id)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                            "Detalle de dispensación no encontrado: " + item.getDispensationDetailId()));

            Long loteId           = ((Number) detalle.get("lote_id")).longValue();
            Long servicioId       = ((Number) detalle.get("servicio_salud_id")).longValue();
            BigDecimal cantidadDispensada = toBigDecimal(detalle.get("cantidad"));
            BigDecimal yaDevuelta = queryRepository
                    .sumReturnedQuantityByDetalleDispensacion(item.getDispensationDetailId(), empresa_id);
            BigDecimal pendiente  = cantidadDispensada.subtract(yaDevuelta);

            if (item.getQuantity().compareTo(pendiente) > 0) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "La cantidad a devolver excede la cantidad dispensada pendiente del detalle "
                                + item.getDispensationDetailId());
            }

            LocalDate vencimiento = toLocalDate(detalle.get("fecha_vencimiento"));
            boolean vencido = vencimiento != null && vencimiento.isBefore(today);

            String tipoMovimiento;
            if (vencido) {
                // Lote vencido: se da de baja sin devolver al stock.
                tipoMovimiento = TIPO_BAJA_VENCIMIENTO;
            } else {
                tipoMovimiento = TIPO_DEVOLUCION_PACIENTE;

                Optional<Map<String, Object>> stockOpt = queryRepository
                        .findStockLoteInBodega(loteId, request.getTargetWarehouseId(), empresa_id);
                StockLoteEntity stock;
                if (stockOpt.isEmpty()) {
                    stock = new StockLoteEntity();
                    stock.setEmpresa_id(empresa_id);
                    stock.setSede_id(sede_id);
                    stock.setBodega_id(request.getTargetWarehouseId());
                    stock.setLote_id(loteId);
                    stock.setCantidad_disponible(item.getQuantity());
                    stock.setCantidad_reservada(BigDecimal.ZERO);
                } else {
                    Long stockId = ((Number) stockOpt.get().get("stock_lote_id")).longValue();
                    stock = stockJpa.findById(stockId)
                            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Stock inconsistente"));
                    stock.setCantidad_disponible(stock.getCantidad_disponible().add(item.getQuantity()));
                }
                stock.setUltimo_movimiento_at(now);
                stockJpa.save(stock);
            }

            MovimientoInventarioEntity mov = new MovimientoInventarioEntity();
            mov.setEmpresa_id(empresa_id);
            mov.setSede_id(sede_id);
            mov.setTipo_movimiento(tipoMovimiento);
            if (vencido) {
                // BAJA_VENCIMIENTO requiere bodega_origen (donde se da de baja).
                mov.setBodega_origen_id(request.getTargetWarehouseId());
            } else {
                mov.setBodega_destino_id(request.getTargetWarehouseId());
            }
            mov.setLote_id(loteId);
            mov.setServicio_salud_id(servicioId);
            mov.setCantidad(item.getQuantity());
            mov.setValor_unitario(BigDecimal.ZERO);
            mov.setReferencia_tipo(REFERENCIA_TIPO);
            mov.setReferencia_id(item.getDispensationDetailId());
            String motivoBase = item.getReason();
            String motivoFinal = vencido
                    ? motivoBase + " [Lote vencido: baja en lugar de devolución]"
                    : motivoBase;
            if (motivoFinal.length() > 300) {
                motivoFinal = motivoFinal.substring(0, 300);
            }
            mov.setMotivo(motivoFinal);
            mov.setFecha_movimiento(now);
            mov.setUsuario_creacion(usuario_id);
            MovimientoInventarioEntity savedMov = movimientoJpa.save(mov);

            response.add(DevolucionItemResponseDto.builder()
                    .movementId(savedMov.getId())
                    .dispensationId(dispensacionId)
                    .dispensationDetailId(item.getDispensationDetailId())
                    .batchId(loteId)
                    .batchNumber((String) detalle.get("numero_lote"))
                    .batchExpirationDate(vencimiento)
                    .healthServiceId(servicioId)
                    .healthServiceName((String) detalle.get("servicio_nombre"))
                    .quantity(item.getQuantity())
                    .targetWarehouseId(request.getTargetWarehouseId())
                    .targetWarehouseName(bodegaNombre)
                    .movementType(tipoMovimiento)
                    .reason(item.getReason())
                    .movementDate(now)
                    .expiredBatchDiscarded(vencido)
                    .build());
        }

        return response;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof Date d) return d.toLocalDate();
        return null;
    }
}

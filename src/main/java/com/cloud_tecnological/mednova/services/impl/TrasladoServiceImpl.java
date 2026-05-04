package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.traslado.CreateTrasladoRequestDto;
import com.cloud_tecnological.mednova.dto.traslado.TrasladoItemRequestDto;
import com.cloud_tecnological.mednova.dto.traslado.TrasladoMovementResponseDto;
import com.cloud_tecnological.mednova.dto.traslado.TrasladoResponseDto;
import com.cloud_tecnological.mednova.entity.MovimientoInventarioEntity;
import com.cloud_tecnological.mednova.entity.StockLoteEntity;
import com.cloud_tecnological.mednova.repositories.movimiento.MovimientoInventarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.stock.StockLoteJpaRepository;
import com.cloud_tecnological.mednova.repositories.traslado.TrasladoQueryRepository;
import com.cloud_tecnological.mednova.services.TrasladoService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class TrasladoServiceImpl implements TrasladoService {

    private static final String TIPO_SALIDA   = "TRASLADO_SALIDA";
    private static final String TIPO_ENTRADA  = "TRASLADO_ENTRADA";
    private static final String REFERENCIA    = "TRASLADO";

    private final TrasladoQueryRepository           queryRepository;
    private final StockLoteJpaRepository            stockJpa;
    private final MovimientoInventarioJpaRepository movimientoJpa;

    public TrasladoServiceImpl(TrasladoQueryRepository queryRepository,
                               StockLoteJpaRepository stockJpa,
                               MovimientoInventarioJpaRepository movimientoJpa) {
        this.queryRepository = queryRepository;
        this.stockJpa        = stockJpa;
        this.movimientoJpa   = movimientoJpa;
    }

    @Override
    @Transactional
    public TrasladoResponseDto transfer(CreateTrasladoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (request.getSourceWarehouseId().equals(request.getTargetWarehouseId())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La bodega de origen no puede ser igual a la de destino");
        }

        Map<String, Object> bodegaOrigen = queryRepository
                .findBodegaInEmpresa(request.getSourceWarehouseId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Bodega de origen no encontrada"));
        if (!Boolean.TRUE.equals(bodegaOrigen.get("permite_dispensar"))) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "La bodega de origen no permite despachar");
        }

        Map<String, Object> bodegaDestino = queryRepository
                .findBodegaInEmpresa(request.getTargetWarehouseId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Bodega de destino no encontrada"));
        if (!Boolean.TRUE.equals(bodegaDestino.get("permite_recibir"))) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "La bodega de destino no permite recibir");
        }

        Long sedeOrigenId  = ((Number) bodegaOrigen.get("sede_id")).longValue();
        Long sedeDestinoId = ((Number) bodegaDestino.get("sede_id")).longValue();
        String origenNombre  = (String) bodegaOrigen.get("nombre");
        String destinoNombre = (String) bodegaDestino.get("nombre");

        // Evitar duplicar el mismo lote en distintos ítems del mismo traslado.
        Set<Long> loteIdsVistos = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today  = LocalDate.now();
        List<TrasladoMovementResponseDto> movements = new ArrayList<>();

        for (TrasladoItemRequestDto item : request.getItems()) {
            if (!loteIdsVistos.add(item.getBatchId())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "El lote " + item.getBatchId() + " aparece más de una vez en el traslado");
            }

            Map<String, Object> lote = queryRepository
                    .findLoteInEmpresa(item.getBatchId(), empresa_id)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                            "Lote no encontrado: " + item.getBatchId()));

            LocalDate vencimiento = toLocalDate(lote.get("fecha_vencimiento"));
            if (vencimiento != null && vencimiento.isBefore(today)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "No se permite trasladar lotes vencidos (lote " + item.getBatchId() + ")");
            }

            Long servicioId = ((Number) lote.get("servicio_salud_id")).longValue();

            Map<String, Object> stockOrigen = queryRepository
                    .findStockLoteInBodega(item.getBatchId(), request.getSourceWarehouseId(), empresa_id)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                            "El lote " + item.getBatchId() + " no tiene stock en la bodega de origen"));

            BigDecimal disponible = toBigDecimal(stockOrigen.get("cantidad_disponible"));
            if (disponible == null || disponible.compareTo(item.getQuantity()) < 0) {
                throw new GlobalException(HttpStatus.CONFLICT,
                        "Stock insuficiente en el lote " + item.getBatchId()
                                + " para trasladar " + item.getQuantity());
            }

            // Descontar stock origen.
            Long stockOrigenId = ((Number) stockOrigen.get("stock_lote_id")).longValue();
            StockLoteEntity stockOrig = stockJpa.findById(stockOrigenId)
                    .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Stock origen inconsistente"));
            stockOrig.setCantidad_disponible(stockOrig.getCantidad_disponible().subtract(item.getQuantity()));
            stockOrig.setUltimo_movimiento_at(now);
            stockJpa.save(stockOrig);

            // Incrementar (o crear) stock destino.
            Optional<Map<String, Object>> stockDestino = queryRepository
                    .findStockLoteInBodega(item.getBatchId(), request.getTargetWarehouseId(), empresa_id);
            StockLoteEntity stockDest;
            if (stockDestino.isEmpty()) {
                stockDest = new StockLoteEntity();
                stockDest.setEmpresa_id(empresa_id);
                stockDest.setSede_id(sedeDestinoId);
                stockDest.setBodega_id(request.getTargetWarehouseId());
                stockDest.setLote_id(item.getBatchId());
                stockDest.setCantidad_disponible(item.getQuantity());
                stockDest.setCantidad_reservada(BigDecimal.ZERO);
            } else {
                Long stockDestId = ((Number) stockDestino.get().get("stock_lote_id")).longValue();
                stockDest = stockJpa.findById(stockDestId)
                        .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Stock destino inconsistente"));
                stockDest.setCantidad_disponible(stockDest.getCantidad_disponible().add(item.getQuantity()));
            }
            stockDest.setUltimo_movimiento_at(now);
            stockJpa.save(stockDest);

            // Movimiento TRASLADO_SALIDA (sede del origen).
            MovimientoInventarioEntity salida = new MovimientoInventarioEntity();
            salida.setEmpresa_id(empresa_id);
            salida.setSede_id(sedeOrigenId);
            salida.setTipo_movimiento(TIPO_SALIDA);
            salida.setBodega_origen_id(request.getSourceWarehouseId());
            salida.setLote_id(item.getBatchId());
            salida.setServicio_salud_id(servicioId);
            salida.setCantidad(item.getQuantity());
            salida.setValor_unitario(BigDecimal.ZERO);
            salida.setReferencia_tipo(REFERENCIA);
            salida.setMotivo(request.getReason());
            salida.setFecha_movimiento(now);
            salida.setUsuario_creacion(usuario_id);
            MovimientoInventarioEntity savedSalida = movimientoJpa.save(salida);

            // Movimiento TRASLADO_ENTRADA (sede del destino), referencia al de salida.
            MovimientoInventarioEntity entrada = new MovimientoInventarioEntity();
            entrada.setEmpresa_id(empresa_id);
            entrada.setSede_id(sedeDestinoId);
            entrada.setTipo_movimiento(TIPO_ENTRADA);
            entrada.setBodega_destino_id(request.getTargetWarehouseId());
            entrada.setLote_id(item.getBatchId());
            entrada.setServicio_salud_id(servicioId);
            entrada.setCantidad(item.getQuantity());
            entrada.setValor_unitario(BigDecimal.ZERO);
            entrada.setReferencia_tipo(REFERENCIA);
            entrada.setReferencia_id(savedSalida.getId());
            entrada.setMotivo(request.getReason());
            entrada.setFecha_movimiento(now);
            entrada.setUsuario_creacion(usuario_id);
            MovimientoInventarioEntity savedEntrada = movimientoJpa.save(entrada);

            movements.add(TrasladoMovementResponseDto.builder()
                    .outboundMovementId(savedSalida.getId())
                    .inboundMovementId(savedEntrada.getId())
                    .batchId(item.getBatchId())
                    .batchNumber((String) lote.get("numero_lote"))
                    .batchExpirationDate(vencimiento)
                    .healthServiceId(servicioId)
                    .healthServiceName((String) lote.get("servicio_nombre"))
                    .quantity(item.getQuantity())
                    .sourceWarehouseId(request.getSourceWarehouseId())
                    .sourceWarehouseName(origenNombre)
                    .targetWarehouseId(request.getTargetWarehouseId())
                    .targetWarehouseName(destinoNombre)
                    .movementDate(now)
                    .build());
        }

        return TrasladoResponseDto.builder()
                .sourceWarehouseId(request.getSourceWarehouseId())
                .sourceWarehouseName(origenNombre)
                .targetWarehouseId(request.getTargetWarehouseId())
                .targetWarehouseName(destinoNombre)
                .transferDate(now)
                .reason(request.getReason())
                .movements(movements)
                .build();
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

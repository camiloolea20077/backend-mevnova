package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.dispensacion.CancelDispensacionRequestDto;
import com.cloud_tecnological.mednova.dto.dispensacion.CreateDetalleDispensacionRequestDto;
import com.cloud_tecnological.mednova.dto.dispensacion.CreateDispensacionRequestDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensacionResponseDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensacionTableDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensationSuggestionItemDto;
import com.cloud_tecnological.mednova.entity.DetalleDispensacionEntity;
import com.cloud_tecnological.mednova.entity.DispensacionEntity;
import com.cloud_tecnological.mednova.entity.MovimientoInventarioEntity;
import com.cloud_tecnological.mednova.entity.StockLoteEntity;
import com.cloud_tecnological.mednova.repositories.dispensacion.DetalleDispensacionJpaRepository;
import com.cloud_tecnological.mednova.repositories.dispensacion.DispensacionJpaRepository;
import com.cloud_tecnological.mednova.repositories.dispensacion.DispensacionQueryRepository;
import com.cloud_tecnological.mednova.repositories.movimiento.MovimientoInventarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.stock.StockLoteJpaRepository;
import com.cloud_tecnological.mednova.services.DispensacionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DispensacionServiceImpl implements DispensacionService {

    private static final String ESTADO_COMPLETA = "COMPLETA";
    private static final String ESTADO_PARCIAL  = "PARCIAL";
    private static final String ESTADO_ANULADA  = "ANULADA";

    private final DispensacionJpaRepository        jpaRepository;
    private final DispensacionQueryRepository      queryRepository;
    private final DetalleDispensacionJpaRepository detalleJpa;
    private final StockLoteJpaRepository           stockJpa;
    private final MovimientoInventarioJpaRepository movimientoJpa;

    public DispensacionServiceImpl(DispensacionJpaRepository jpaRepository,
                                   DispensacionQueryRepository queryRepository,
                                   DetalleDispensacionJpaRepository detalleJpa,
                                   StockLoteJpaRepository stockJpa,
                                   MovimientoInventarioJpaRepository movimientoJpa) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
        this.detalleJpa      = detalleJpa;
        this.stockJpa        = stockJpa;
        this.movimientoJpa   = movimientoJpa;
    }

    // ── HU-FASE2-074: Sugerencias FEFO por prescripción y bodega ─────────────

    @Override
    public List<DispensationSuggestionItemDto> getSuggestions(Long prescripcionId, Long bodegaId) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();

        if (!queryRepository.existsBodegaActivaPermiteDispensar(bodegaId, empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Bodega no encontrada o no permite dispensar");
        }
        queryRepository.findActivePrescripcion(prescripcionId, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Prescripción no encontrada o no activa"));

        return queryRepository.listDispensationSuggestions(prescripcionId, bodegaId, empresa_id);
    }

    // ── HU-FASE2-074: Crear dispensación ─────────────────────────────────────

    @Override
    @Transactional
    public DispensacionResponseDto create(CreateDispensacionRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (!queryRepository.existsBodegaActivaPermiteDispensar(request.getWarehouseId(), empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Bodega no encontrada o no permite dispensar");
        }
        if (!queryRepository.existsProfesionalByEmpresa(request.getDispensingProfessionalId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional dispensador no encontrado");
        }
        if (request.getReceivingProfessionalId() != null
                && !queryRepository.existsProfesionalByEmpresa(request.getReceivingProfessionalId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional receptor no encontrado");
        }

        Map<String, Object> prescripcion = queryRepository
                .findActivePrescripcion(request.getPrescriptionId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Prescripción no encontrada, no activa o de otra sede"));

        Long pacienteId = ((Number) prescripcion.get("paciente_id")).longValue();

        // Crear encabezado.
        DispensacionEntity dispensacion = new DispensacionEntity();
        dispensacion.setEmpresa_id(empresa_id);
        dispensacion.setSede_id(sede_id);
        dispensacion.setBodega_id(request.getWarehouseId());
        dispensacion.setNumero_dispensacion(queryRepository.generateNextNumeroDispensacion(empresa_id));
        dispensacion.setPrescripcion_id(request.getPrescriptionId());
        dispensacion.setPaciente_id(pacienteId);
        dispensacion.setProfesional_dispensador_id(request.getDispensingProfessionalId());
        dispensacion.setProfesional_receptor_id(request.getReceivingProfessionalId());
        dispensacion.setEstado(ESTADO_COMPLETA);
        dispensacion.setObservaciones(request.getObservations());
        dispensacion.setUsuario_creacion(usuario_id);
        DispensacionEntity saved = jpaRepository.save(dispensacion);

        LocalDateTime now = LocalDateTime.now();
        boolean parcial = false;

        // Procesar cada ítem.
        for (CreateDetalleDispensacionRequestDto item : request.getItems()) {
            Map<String, Object> detallePrescripcion = queryRepository
                    .findDetallePrescripcion(item.getPrescriptionDetailId(), request.getPrescriptionId(), empresa_id)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                            "Detalle de prescripción no encontrado: " + item.getPrescriptionDetailId()));

            Long servicioId         = ((Number) detallePrescripcion.get("servicio_salud_id")).longValue();
            BigDecimal cantidadPrescrita = toBigDecimal(detallePrescripcion.get("cantidad_despachar"));
            BigDecimal yaDispensada      = queryRepository
                    .sumDispensedQuantityByDetallePrescripcion(item.getPrescriptionDetailId(), empresa_id);
            BigDecimal pendiente = cantidadPrescrita == null
                    ? null
                    : cantidadPrescrita.subtract(yaDispensada == null ? BigDecimal.ZERO : yaDispensada);

            if (cantidadPrescrita != null && item.getQuantity().compareTo(pendiente) > 0) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "La cantidad a dispensar excede la cantidad pendiente del detalle "
                                + item.getPrescriptionDetailId());
            }

            // Resolver lote (override o FEFO).
            Optional<Map<String, Object>> fefo = queryRepository
                    .findFefoLoteSuggestion(servicioId, request.getWarehouseId(), empresa_id);
            Long loteId = item.getBatchId();
            if (loteId == null) {
                if (fefo.isEmpty()) {
                    throw new GlobalException(HttpStatus.CONFLICT,
                            "Sin stock disponible no vencido para el servicio " + servicioId
                                    + " en la bodega seleccionada");
                }
                loteId = ((Number) fefo.get().get("lote_id")).longValue();
            } else {
                Long fefoLoteId = fefo.map(m -> ((Number) m.get("lote_id")).longValue()).orElse(null);
                if (fefoLoteId != null && !loteId.equals(fefoLoteId)
                        && (item.getOverrideReason() == null || item.getOverrideReason().isBlank())) {
                    throw new GlobalException(HttpStatus.BAD_REQUEST,
                            "Se requiere justificación al cambiar manualmente el lote sugerido por FEFO");
                }
            }

            // Validar stock del lote en la bodega.
            Map<String, Object> stockOrigen = queryRepository
                    .findStockLoteInBodega(loteId, request.getWarehouseId(), empresa_id)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                            "El lote no tiene stock en la bodega seleccionada"));

            Long servicioLote = ((Number) stockOrigen.get("servicio_salud_id")).longValue();
            if (!servicioLote.equals(servicioId)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "El lote no corresponde al servicio prescrito");
            }

            LocalDate vencimiento = toLocalDate(stockOrigen.get("fecha_vencimiento"));
            if (vencimiento != null && vencimiento.isBefore(LocalDate.now())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "No se permite dispensar con lotes vencidos");
            }

            BigDecimal disponible = toBigDecimal(stockOrigen.get("cantidad_disponible"));
            if (disponible == null || disponible.compareTo(item.getQuantity()) < 0) {
                throw new GlobalException(HttpStatus.CONFLICT,
                        "Stock insuficiente en el lote seleccionado para dispensar " + item.getQuantity());
            }

            // Descontar stock.
            Long stockOrigenId = ((Number) stockOrigen.get("stock_lote_id")).longValue();
            StockLoteEntity stock = stockJpa.findById(stockOrigenId)
                    .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Stock inconsistente"));
            stock.setCantidad_disponible(stock.getCantidad_disponible().subtract(item.getQuantity()));
            stock.setUltimo_movimiento_at(now);
            stockJpa.save(stock);

            // Crear movimiento_inventario tipo SALIDA_DISPENSACION.
            MovimientoInventarioEntity mov = new MovimientoInventarioEntity();
            mov.setEmpresa_id(empresa_id);
            mov.setSede_id(sede_id);
            mov.setTipo_movimiento("SALIDA_DISPENSACION");
            mov.setBodega_origen_id(request.getWarehouseId());
            mov.setLote_id(loteId);
            mov.setServicio_salud_id(servicioId);
            mov.setCantidad(item.getQuantity());
            mov.setValor_unitario(BigDecimal.ZERO);
            mov.setReferencia_tipo("DISPENSACION");
            mov.setReferencia_id(saved.getId());
            mov.setMotivo("Dispensación " + saved.getNumero_dispensacion());
            mov.setFecha_movimiento(now);
            mov.setUsuario_creacion(usuario_id);
            movimientoJpa.save(mov);

            // Crear detalle_dispensacion.
            DetalleDispensacionEntity detalle = new DetalleDispensacionEntity();
            detalle.setEmpresa_id(empresa_id);
            detalle.setDispensacion_id(saved.getId());
            detalle.setDetalle_prescripcion_id(item.getPrescriptionDetailId());
            detalle.setServicio_salud_id(servicioId);
            detalle.setLote_id(loteId);
            detalle.setCantidad(item.getQuantity());
            detalle.setObservaciones(item.getObservations());
            detalleJpa.save(detalle);

            // Recalcular pendiente para marcar parcial.
            if (cantidadPrescrita != null) {
                BigDecimal nuevoTotal = (yaDispensada == null ? BigDecimal.ZERO : yaDispensada).add(item.getQuantity());
                if (nuevoTotal.compareTo(cantidadPrescrita) < 0) {
                    parcial = true;
                }
            }
        }

        if (parcial) {
            saved.setEstado(ESTADO_PARCIAL);
            saved.setUsuario_modificacion(usuario_id);
            jpaRepository.save(saved);
        }

        return queryRepository.findActiveById(saved.getId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la dispensación creada"));
    }

    @Override
    public DispensacionResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return queryRepository.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Dispensación no encontrada"));
    }

    @Override
    public PageImpl<DispensacionTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return queryRepository.listDispensaciones(pageable, empresa_id, sede_id);
    }

    /**
     * Anula una dispensación: revierte el stock al lote y crea movimiento DEVOLUCION_PACIENTE inverso.
     * Solo se puede anular una dispensación COMPLETA o PARCIAL.
     */
    @Override
    @Transactional
    public DispensacionResponseDto cancel(Long id, CancelDispensacionRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        DispensacionEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (ESTADO_ANULADA.equals(entity.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT, "La dispensación ya fue anulada");
        }

        LocalDateTime now = LocalDateTime.now();

        // Revertir stock por cada detalle no eliminado.
        List<DetalleDispensacionEntity> detalles = queryRepository
                .findItemsByDispensacion(id, empresa_id)
                .stream()
                .map(dto -> detalleJpa.findById(dto.getId())
                        .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Detalle de dispensación inconsistente")))
                .toList();

        for (DetalleDispensacionEntity det : detalles) {
            Map<String, Object> stock = queryRepository
                    .findStockLoteInBodega(det.getLote_id(), entity.getBodega_id(), empresa_id)
                    .orElse(null);
            StockLoteEntity stockEntity;
            if (stock == null) {
                stockEntity = new StockLoteEntity();
                stockEntity.setEmpresa_id(empresa_id);
                stockEntity.setSede_id(sede_id);
                stockEntity.setBodega_id(entity.getBodega_id());
                stockEntity.setLote_id(det.getLote_id());
                stockEntity.setCantidad_disponible(det.getCantidad());
                stockEntity.setCantidad_reservada(BigDecimal.ZERO);
            } else {
                Long stockId = ((Number) stock.get("stock_lote_id")).longValue();
                stockEntity = stockJpa.findById(stockId)
                        .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Stock inconsistente"));
                stockEntity.setCantidad_disponible(stockEntity.getCantidad_disponible().add(det.getCantidad()));
            }
            stockEntity.setUltimo_movimiento_at(now);
            stockJpa.save(stockEntity);

            MovimientoInventarioEntity mov = new MovimientoInventarioEntity();
            mov.setEmpresa_id(empresa_id);
            mov.setSede_id(sede_id);
            mov.setTipo_movimiento("DEVOLUCION_PACIENTE");
            mov.setBodega_destino_id(entity.getBodega_id());
            mov.setLote_id(det.getLote_id());
            mov.setServicio_salud_id(det.getServicio_salud_id());
            mov.setCantidad(det.getCantidad());
            mov.setValor_unitario(BigDecimal.ZERO);
            mov.setReferencia_tipo("DISPENSACION");
            mov.setReferencia_id(entity.getId());
            mov.setMotivo("Anulación dispensación " + entity.getNumero_dispensacion());
            mov.setFecha_movimiento(now);
            mov.setUsuario_creacion(usuario_id);
            movimientoJpa.save(mov);
        }

        String observaciones = entity.getObservaciones() == null ? "" : entity.getObservaciones() + "\n";
        entity.setObservaciones(observaciones + "[ANULACIÓN] " + request.getReason());
        entity.setEstado(ESTADO_ANULADA);
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar la dispensación"));
    }

    private DispensacionEntity getValidEntity(Long id, Long empresa_id, Long sede_id) {
        DispensacionEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Dispensación no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id())
                || !sede_id.equals(entity.getSede_id())
                || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Dispensación no encontrada");
        }
        return entity;
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

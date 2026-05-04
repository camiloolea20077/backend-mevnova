package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.solicitudmedicamento.CancelSolicitudMedicamentoRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.CreateDetalleSolicitudMedicamentoRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.CreateSolicitudMedicamentoRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DispatchSolicitudItemRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DispatchSolicitudRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DispatchSuggestionItemDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.SolicitudMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.SolicitudMedicamentoTableDto;
import com.cloud_tecnological.mednova.entity.DetalleSolicitudMedicamentoEntity;
import com.cloud_tecnological.mednova.entity.MovimientoInventarioEntity;
import com.cloud_tecnological.mednova.entity.SolicitudMedicamentoEntity;
import com.cloud_tecnological.mednova.entity.StockLoteEntity;
import com.cloud_tecnological.mednova.repositories.movimiento.MovimientoInventarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.solicitudmedicamento.DetalleSolicitudMedicamentoJpaRepository;
import com.cloud_tecnological.mednova.repositories.solicitudmedicamento.SolicitudMedicamentoJpaRepository;
import com.cloud_tecnological.mednova.repositories.solicitudmedicamento.SolicitudMedicamentoQueryRepository;
import com.cloud_tecnological.mednova.repositories.stock.StockLoteJpaRepository;
import com.cloud_tecnological.mednova.services.SolicitudMedicamentoService;
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
public class SolicitudMedicamentoServiceImpl implements SolicitudMedicamentoService {

    private static final String ESTADO_PENDIENTE   = "PENDIENTE";
    private static final String ESTADO_EN_PROCESO  = "EN_PROCESO";
    private static final String ESTADO_ANULADA     = "ANULADA";
    private static final String ESTADO_DESPACHADA  = "DESPACHADA";
    private static final String ESTADO_PARCIAL     = "PARCIAL";
    private static final String ESTADO_RECHAZADA   = "RECHAZADA";
    private static final String ESTADO_DESPACHADO  = "DESPACHADO";
    private static final String ESTADO_RECHAZADO   = "RECHAZADO";

    private final SolicitudMedicamentoJpaRepository        jpaRepository;
    private final SolicitudMedicamentoQueryRepository      queryRepository;
    private final DetalleSolicitudMedicamentoJpaRepository detalleJpa;
    private final StockLoteJpaRepository                   stockJpa;
    private final MovimientoInventarioJpaRepository        movimientoJpa;

    public SolicitudMedicamentoServiceImpl(SolicitudMedicamentoJpaRepository jpaRepository,
                                           SolicitudMedicamentoQueryRepository queryRepository,
                                           DetalleSolicitudMedicamentoJpaRepository detalleJpa,
                                           StockLoteJpaRepository stockJpa,
                                           MovimientoInventarioJpaRepository movimientoJpa) {
        this.jpaRepository   = jpaRepository;
        this.queryRepository = queryRepository;
        this.detalleJpa      = detalleJpa;
        this.stockJpa        = stockJpa;
        this.movimientoJpa   = movimientoJpa;
    }

    @Override
    @Transactional
    public SolicitudMedicamentoResponseDto create(CreateSolicitudMedicamentoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        if (request.getSourceWarehouseId().equals(request.getDestinationWarehouseId())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "La bodega de origen no puede ser igual a la de destino");
        }

        if (!queryRepository.existsBodegaActivaPermiteDispensar(request.getSourceWarehouseId(), empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND,
                    "Bodega de origen no encontrada o no permite despachar");
        }
        if (!queryRepository.existsBodegaActivaPermiteRecibir(request.getDestinationWarehouseId(), empresa_id, sede_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND,
                    "Bodega de destino no encontrada o no permite recibir");
        }
        if (request.getRequestingProfessionalId() != null
                && !queryRepository.existsProfesionalByEmpresa(request.getRequestingProfessionalId(), empresa_id)) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Profesional solicitante no encontrado");
        }

        for (CreateDetalleSolicitudMedicamentoRequestDto item : request.getItems()) {
            if (!queryRepository.isServicioMedicamentoOInsumo(item.getHealthServiceId(), empresa_id)) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "El servicio " + item.getHealthServiceId() + " no es de categoría MEDICAMENTO o INSUMO");
            }
        }

        SolicitudMedicamentoEntity solicitud = new SolicitudMedicamentoEntity();
        solicitud.setEmpresa_id(empresa_id);
        solicitud.setSede_id(sede_id);
        solicitud.setNumero_solicitud(queryRepository.generateNextNumeroSolicitud(empresa_id));
        solicitud.setBodega_origen_id(request.getSourceWarehouseId());
        solicitud.setBodega_destino_id(request.getDestinationWarehouseId());
        solicitud.setProfesional_solicitante_id(request.getRequestingProfessionalId());
        solicitud.setEstado_solicitud(ESTADO_PENDIENTE);
        solicitud.setPrioridad(request.getPriority() == null ? "NORMAL" : request.getPriority());
        solicitud.setMotivo(request.getReason());
        solicitud.setObservaciones(request.getObservations());
        solicitud.setUsuario_creacion(usuario_id);
        SolicitudMedicamentoEntity saved = jpaRepository.save(solicitud);

        for (CreateDetalleSolicitudMedicamentoRequestDto item : request.getItems()) {
            DetalleSolicitudMedicamentoEntity detalle = new DetalleSolicitudMedicamentoEntity();
            detalle.setEmpresa_id(empresa_id);
            detalle.setSolicitud_id(saved.getId());
            detalle.setServicio_salud_id(item.getHealthServiceId());
            detalle.setCantidad_solicitada(item.getRequestedQuantity());
            detalle.setCantidad_despachada(BigDecimal.ZERO);
            detalle.setEstado(ESTADO_PENDIENTE);
            detalleJpa.save(detalle);
        }

        return queryRepository.findActiveById(saved.getId(), empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la solicitud creada"));
    }

    @Override
    public SolicitudMedicamentoResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return queryRepository.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
    }

    @Override
    public PageImpl<SolicitudMedicamentoTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return queryRepository.listSolicitudes(pageable, empresa_id, sede_id);
    }

    @Override
    @Transactional
    public SolicitudMedicamentoResponseDto cancel(Long id, CancelSolicitudMedicamentoRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        SolicitudMedicamentoEntity entity = getValidEntity(id, empresa_id, sede_id);
        if (!ESTADO_PENDIENTE.equals(entity.getEstado_solicitud())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo solicitudes en estado PENDIENTE pueden anularse");
        }
        if (ESTADO_DESPACHADA.equals(entity.getEstado_solicitud())
                || ESTADO_PARCIAL.equals(entity.getEstado_solicitud())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Una solicitud ya despachada no puede anularse");
        }

        String observaciones = entity.getObservaciones() == null ? "" : entity.getObservaciones() + "\n";
        entity.setObservaciones(observaciones + "[ANULACIÓN] " + request.getReason());
        entity.setEstado_solicitud(ESTADO_ANULADA);
        entity.setUsuario_modificacion(usuario_id);
        jpaRepository.save(entity);

        return queryRepository.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la solicitud"));
    }

    // ── HU-FASE2-073: Despacho FEFO ─────────────────────────────────────────

    @Override
    public List<DispatchSuggestionItemDto> getDispatchSuggestions(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        // Valida cross-tenant y existencia.
        getValidEntity(id, empresa_id, sede_id);
        return queryRepository.listDispatchSuggestions(id, empresa_id);
    }

    @Override
    @Transactional
    public SolicitudMedicamentoResponseDto dispatch(Long id, DispatchSolicitudRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        Long usuario_id = TenantContext.getUsuarioId();

        SolicitudMedicamentoEntity solicitud = getValidEntity(id, empresa_id, sede_id);
        if (!ESTADO_PENDIENTE.equals(solicitud.getEstado_solicitud())
                && !ESTADO_EN_PROCESO.equals(solicitud.getEstado_solicitud())
                && !ESTADO_PARCIAL.equals(solicitud.getEstado_solicitud())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo solicitudes PENDIENTE, EN_PROCESO o PARCIAL pueden despacharse");
        }

        Long bodegaOrigenId = solicitud.getBodega_origen_id();
        Long bodegaDestinoId = solicitud.getBodega_destino_id();
        LocalDateTime now = LocalDateTime.now();

        for (DispatchSolicitudItemRequestDto item : request.getItems()) {
            DetalleSolicitudMedicamentoEntity detalle = detalleJpa.findById(item.getDetailId())
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                            "Detalle de solicitud no encontrado: " + item.getDetailId()));
            if (!empresa_id.equals(detalle.getEmpresa_id())
                    || !id.equals(detalle.getSolicitud_id())
                    || detalle.getDeleted_at() != null) {
                throw new GlobalException(HttpStatus.NOT_FOUND,
                        "Detalle de solicitud no encontrado: " + item.getDetailId());
            }
            if (!ESTADO_PENDIENTE.equals(detalle.getEstado()) && !ESTADO_PARCIAL.equals(detalle.getEstado())) {
                throw new GlobalException(HttpStatus.CONFLICT,
                        "El detalle " + detalle.getId() + " ya fue procesado (estado " + detalle.getEstado() + ")");
            }

            // Resolver lote: explícito o sugerencia FEFO.
            Long loteId = item.getBatchId();
            Optional<Map<String, Object>> fefo = queryRepository.findFefoLoteSuggestion(
                    detalle.getServicio_salud_id(), bodegaOrigenId, empresa_id);

            if (loteId == null) {
                if (fefo.isEmpty()) {
                    detalle.setEstado(ESTADO_RECHAZADO);
                    detalle.setMotivo_rechazo("Sin stock disponible no vencido en bodega origen");
                    detalleJpa.save(detalle);
                    continue;
                }
                loteId = ((Number) fefo.get().get("lote_id")).longValue();
            } else {
                // Override: si el lote elegido difiere de FEFO, se exige justificación.
                Long fefoLoteId = fefo.map(m -> ((Number) m.get("lote_id")).longValue()).orElse(null);
                if (fefoLoteId != null && !loteId.equals(fefoLoteId)
                        && (item.getOverrideReason() == null || item.getOverrideReason().isBlank())) {
                    throw new GlobalException(HttpStatus.BAD_REQUEST,
                            "Se requiere justificación al cambiar manualmente el lote sugerido por FEFO");
                }
            }

            // Validar lote en bodega origen.
            Optional<Map<String, Object>> stockOrigen = queryRepository.findStockLoteInBodega(loteId, bodegaOrigenId, empresa_id);
            if (stockOrigen.isEmpty()) {
                throw new GlobalException(HttpStatus.NOT_FOUND,
                        "El lote " + loteId + " no tiene stock en la bodega de origen");
            }

            LocalDate vencimiento = toLocalDate(stockOrigen.get().get("fecha_vencimiento"));
            if (vencimiento != null && vencimiento.isBefore(LocalDate.now())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "No se permite despacho con lotes vencidos (lote " + loteId + ")");
            }

            BigDecimal disponible = toBigDecimal(stockOrigen.get().get("cantidad_disponible"));
            BigDecimal solicitada = item.getQuantityToDispatch();
            BigDecimal pendiente  = detalle.getCantidad_solicitada().subtract(detalle.getCantidad_despachada());
            // No despachar más de lo pendiente del detalle.
            if (solicitada.compareTo(pendiente) > 0) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "La cantidad a despachar excede la pendiente del detalle " + detalle.getId());
            }
            BigDecimal aDespachar = solicitada.min(disponible);
            if (aDespachar.signum() <= 0) {
                detalle.setEstado(ESTADO_RECHAZADO);
                detalle.setMotivo_rechazo("Sin stock disponible en lote " + loteId);
                detalleJpa.save(detalle);
                continue;
            }

            // Descontar stock origen.
            Long stockOrigenId = ((Number) stockOrigen.get().get("stock_lote_id")).longValue();
            StockLoteEntity stockOrig = stockJpa.findById(stockOrigenId)
                    .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Stock origen inconsistente"));
            stockOrig.setCantidad_disponible(stockOrig.getCantidad_disponible().subtract(aDespachar));
            stockOrig.setUltimo_movimiento_at(now);
            stockJpa.save(stockOrig);

            // Incrementar stock destino (crea fila si no existe).
            Optional<Map<String, Object>> stockDestino = queryRepository.findStockLoteInBodega(loteId, bodegaDestinoId, empresa_id);
            StockLoteEntity stockDest;
            if (stockDestino.isEmpty()) {
                stockDest = new StockLoteEntity();
                stockDest.setEmpresa_id(empresa_id);
                stockDest.setSede_id(sede_id);
                stockDest.setBodega_id(bodegaDestinoId);
                stockDest.setLote_id(loteId);
                stockDest.setCantidad_disponible(aDespachar);
                stockDest.setCantidad_reservada(BigDecimal.ZERO);
            } else {
                Long stockDestId = ((Number) stockDestino.get().get("stock_lote_id")).longValue();
                stockDest = stockJpa.findById(stockDestId)
                        .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Stock destino inconsistente"));
                stockDest.setCantidad_disponible(stockDest.getCantidad_disponible().add(aDespachar));
            }
            stockDest.setUltimo_movimiento_at(now);
            stockJpa.save(stockDest);

            // Movimientos: TRASLADO_SALIDA (origen) + TRASLADO_ENTRADA (destino).
            createMovimiento("TRASLADO_SALIDA", bodegaOrigenId, null, loteId,
                    detalle.getServicio_salud_id(), aDespachar, solicitud, sede_id, empresa_id, usuario_id, now);
            createMovimiento("TRASLADO_ENTRADA", null, bodegaDestinoId, loteId,
                    detalle.getServicio_salud_id(), aDespachar, solicitud, sede_id, empresa_id, usuario_id, now);

            // Actualizar detalle.
            BigDecimal totalDespachado = detalle.getCantidad_despachada().add(aDespachar);
            detalle.setCantidad_despachada(totalDespachado);
            if (totalDespachado.compareTo(detalle.getCantidad_solicitada()) >= 0) {
                detalle.setEstado(ESTADO_DESPACHADO);
                detalle.setMotivo_rechazo(null);
            } else {
                detalle.setEstado(ESTADO_PARCIAL);
                detalle.setMotivo_rechazo("Despacho parcial: stock insuficiente");
            }
            detalleJpa.save(detalle);
        }

        // Estado del encabezado según los detalles finales.
        Map<String, Long> conteo = queryRepository.findItemsBySolicitud(id, empresa_id).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        d -> d.getState() == null ? "PENDIENTE" : d.getState(),
                        java.util.stream.Collectors.counting()));

        long despachados = conteo.getOrDefault(ESTADO_DESPACHADO, 0L);
        long parciales   = conteo.getOrDefault(ESTADO_PARCIAL, 0L);
        long rechazados  = conteo.getOrDefault(ESTADO_RECHAZADO, 0L);
        long pendientes  = conteo.getOrDefault(ESTADO_PENDIENTE, 0L);
        long total       = despachados + parciales + rechazados + pendientes;

        String nuevoEstado;
        if (total > 0 && rechazados == total) {
            nuevoEstado = ESTADO_RECHAZADA;
        } else if (pendientes > 0 || parciales > 0) {
            nuevoEstado = (despachados > 0 || rechazados > 0) ? ESTADO_PARCIAL : ESTADO_EN_PROCESO;
        } else if (despachados > 0 && rechazados == 0) {
            nuevoEstado = ESTADO_DESPACHADA;
        } else {
            nuevoEstado = ESTADO_PARCIAL;
        }

        solicitud.setEstado_solicitud(nuevoEstado);
        if (ESTADO_DESPACHADA.equals(nuevoEstado) || ESTADO_PARCIAL.equals(nuevoEstado) || ESTADO_RECHAZADA.equals(nuevoEstado)) {
            solicitud.setFecha_despacho(now);
        }
        solicitud.setUsuario_modificacion(usuario_id);
        jpaRepository.save(solicitud);

        return queryRepository.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la solicitud"));
    }

    private void createMovimiento(String tipo, Long bodegaOrigen, Long bodegaDestino, Long loteId,
                                  Long servicioId, BigDecimal cantidad,
                                  SolicitudMedicamentoEntity solicitud, Long sede_id, Long empresa_id,
                                  Long usuario_id, LocalDateTime when) {
        MovimientoInventarioEntity mov = new MovimientoInventarioEntity();
        mov.setEmpresa_id(empresa_id);
        mov.setSede_id(sede_id);
        mov.setTipo_movimiento(tipo);
        mov.setBodega_origen_id(bodegaOrigen);
        mov.setBodega_destino_id(bodegaDestino);
        mov.setLote_id(loteId);
        mov.setServicio_salud_id(servicioId);
        mov.setCantidad(cantidad);
        mov.setValor_unitario(BigDecimal.ZERO);
        mov.setReferencia_tipo("SOLICITUD");
        mov.setReferencia_id(solicitud.getId());
        mov.setMotivo("Despacho solicitud " + solicitud.getNumero_solicitud());
        mov.setFecha_movimiento(when);
        mov.setUsuario_creacion(usuario_id);
        movimientoJpa.save(mov);
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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private SolicitudMedicamentoEntity getValidEntity(Long id, Long empresa_id, Long sede_id) {
        SolicitudMedicamentoEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        if (!empresa_id.equals(entity.getEmpresa_id())
                || !sede_id.equals(entity.getSede_id())
                || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Solicitud no encontrada");
        }
        return entity;
    }
}

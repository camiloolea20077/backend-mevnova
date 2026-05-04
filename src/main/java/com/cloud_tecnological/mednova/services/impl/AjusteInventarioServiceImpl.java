package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.ajuste.AjusteInventarioResponseDto;
import com.cloud_tecnological.mednova.dto.ajuste.AjusteInventarioTableDto;
import com.cloud_tecnological.mednova.dto.ajuste.CancelAjusteRequestDto;
import com.cloud_tecnological.mednova.dto.ajuste.CreateAjusteInventarioRequestDto;
import com.cloud_tecnological.mednova.dto.ajuste.CreateDetalleAjusteRequestDto;
import com.cloud_tecnological.mednova.entity.AjusteInventarioEntity;
import com.cloud_tecnological.mednova.entity.DetalleAjusteInventarioEntity;
import com.cloud_tecnological.mednova.entity.MovimientoInventarioEntity;
import com.cloud_tecnological.mednova.entity.StockLoteEntity;
import com.cloud_tecnological.mednova.repositories.ajuste.AjusteInventarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.ajuste.AjusteInventarioQueryRepository;
import com.cloud_tecnological.mednova.repositories.ajuste.DetalleAjusteInventarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.movimiento.MovimientoInventarioJpaRepository;
import com.cloud_tecnological.mednova.repositories.stock.StockLoteJpaRepository;
import com.cloud_tecnological.mednova.services.AjusteInventarioService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AjusteInventarioServiceImpl implements AjusteInventarioService {

    private static final String ESTADO_BORRADOR = "BORRADOR";
    private static final String ESTADO_APROBADO = "APROBADO";
    private static final String ESTADO_APLICADO = "APLICADO";
    private static final String ESTADO_ANULADO  = "ANULADO";

    private static final String TIPO_AJUSTE_POSITIVO = "AJUSTE_POSITIVO";
    private static final String TIPO_AJUSTE_NEGATIVO = "AJUSTE_NEGATIVO";
    private static final String REFERENCIA_AJUSTE   = "AJUSTE";

    private final AjusteInventarioJpaRepository       ajusteJpa;
    private final AjusteInventarioQueryRepository     ajusteQuery;
    private final DetalleAjusteInventarioJpaRepository detalleJpa;
    private final StockLoteJpaRepository              stockJpa;
    private final MovimientoInventarioJpaRepository   movimientoJpa;

    public AjusteInventarioServiceImpl(AjusteInventarioJpaRepository ajusteJpa,
                                       AjusteInventarioQueryRepository ajusteQuery,
                                       DetalleAjusteInventarioJpaRepository detalleJpa,
                                       StockLoteJpaRepository stockJpa,
                                       MovimientoInventarioJpaRepository movimientoJpa) {
        this.ajusteJpa     = ajusteJpa;
        this.ajusteQuery   = ajusteQuery;
        this.detalleJpa    = detalleJpa;
        this.stockJpa      = stockJpa;
        this.movimientoJpa = movimientoJpa;
    }

    @Override
    @Transactional
    public AjusteInventarioResponseDto create(CreateAjusteInventarioRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        Map<String, Object> bodega = ajusteQuery
                .findBodegaInEmpresa(request.getWarehouseId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Bodega no encontrada"));
        Long sedeBodegaId = ((Number) bodega.get("sede_id")).longValue();

        AjusteInventarioEntity ajuste = new AjusteInventarioEntity();
        ajuste.setEmpresa_id(empresa_id);
        ajuste.setSede_id(sedeBodegaId);
        ajuste.setBodega_id(request.getWarehouseId());
        ajuste.setNumero_ajuste(ajusteQuery.generateNextNumeroAjuste(empresa_id));
        ajuste.setTipo_ajuste(request.getAdjustmentType());
        ajuste.setFecha_ajuste(request.getAdjustmentDate());
        ajuste.setMotivo(request.getReason());
        ajuste.setEstado(ESTADO_BORRADOR);
        ajuste.setUsuario_creacion(usuario_id);
        AjusteInventarioEntity saved = ajusteJpa.save(ajuste);

        BigDecimal valorTotal = BigDecimal.ZERO;
        Set<Long> lotesVistos = new HashSet<>();

        for (CreateDetalleAjusteRequestDto item : request.getItems()) {
            if (!lotesVistos.add(item.getBatchId())) {
                throw new GlobalException(HttpStatus.BAD_REQUEST,
                        "El lote " + item.getBatchId() + " aparece más de una vez en el ajuste");
            }

            Map<String, Object> lote = ajusteQuery
                    .findLoteInEmpresa(item.getBatchId(), empresa_id)
                    .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                            "Lote no encontrado: " + item.getBatchId()));
            Long servicioId = ((Number) lote.get("servicio_salud_id")).longValue();

            BigDecimal diferencia = item.getRealQuantity().subtract(item.getSystemQuantity());
            BigDecimal valorDiferencia = diferencia
                    .multiply(item.getUnitValue())
                    .setScale(2, RoundingMode.HALF_UP);

            DetalleAjusteInventarioEntity detalle = new DetalleAjusteInventarioEntity();
            detalle.setEmpresa_id(empresa_id);
            detalle.setAjuste_id(saved.getId());
            detalle.setLote_id(item.getBatchId());
            detalle.setServicio_salud_id(servicioId);
            detalle.setCantidad_sistema(item.getSystemQuantity());
            detalle.setCantidad_real(item.getRealQuantity());
            detalle.setValor_unitario(item.getUnitValue());
            detalle.setValor_diferencia(valorDiferencia);
            detalle.setObservaciones(item.getObservations());
            detalleJpa.save(detalle);

            valorTotal = valorTotal.add(valorDiferencia);
        }

        saved.setValor_total_ajuste(valorTotal);
        ajusteJpa.save(saved);

        return ajusteQuery.findActiveById(saved.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el ajuste creado"));
    }

    @Override
    public AjusteInventarioResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        return ajusteQuery.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Ajuste no encontrado"));
    }

    @Override
    public PageImpl<AjusteInventarioTableDto> list(PageableDto<?> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return ajusteQuery.listAjustes(pageable, empresa_id);
    }

    @Override
    @Transactional
    public AjusteInventarioResponseDto approve(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AjusteInventarioEntity ajuste = getValidEntity(id, empresa_id);
        if (!ESTADO_BORRADOR.equals(ajuste.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo los ajustes en estado BORRADOR pueden aprobarse");
        }
        // Segregación de funciones: aprobador distinto del creador.
        if (ajuste.getUsuario_creacion() != null && ajuste.getUsuario_creacion().equals(usuario_id)) {
            throw new GlobalException(HttpStatus.FORBIDDEN,
                    "El aprobador debe ser un usuario distinto al creador del ajuste");
        }
        if (!ajusteQuery.existsUsuarioActivo(usuario_id, empresa_id)) {
            throw new GlobalException(HttpStatus.FORBIDDEN, "Usuario aprobador no válido");
        }

        ajuste.setEstado(ESTADO_APROBADO);
        ajuste.setAprobado_por_id(usuario_id);
        ajuste.setFecha_aprobacion(LocalDateTime.now());
        ajuste.setUsuario_modificacion(usuario_id);
        ajusteJpa.save(ajuste);

        return ajusteQuery.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el ajuste"));
    }

    @Override
    @Transactional
    public AjusteInventarioResponseDto apply(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AjusteInventarioEntity ajuste = getValidEntity(id, empresa_id);
        if (!ESTADO_APROBADO.equals(ajuste.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Solo los ajustes APROBADOS pueden aplicarse");
        }

        List<DetalleAjusteInventarioEntity> detalles = findDetallesByAjuste(ajuste.getId(), empresa_id);
        if (detalles.isEmpty()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "El ajuste no tiene ítems registrados");
        }

        LocalDateTime now = LocalDateTime.now();
        for (DetalleAjusteInventarioEntity detalle : detalles) {
            applyDetalleStockAndMovement(detalle, ajuste, empresa_id, usuario_id, now);
        }

        ajuste.setEstado(ESTADO_APLICADO);
        ajuste.setUsuario_modificacion(usuario_id);
        ajusteJpa.save(ajuste);

        return ajusteQuery.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el ajuste"));
    }

    @Override
    @Transactional
    public AjusteInventarioResponseDto cancel(Long id, CancelAjusteRequestDto request) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long usuario_id = TenantContext.getUsuarioId();

        AjusteInventarioEntity ajuste = getValidEntity(id, empresa_id);
        if (ESTADO_ANULADO.equals(ajuste.getEstado())) {
            throw new GlobalException(HttpStatus.CONFLICT, "El ajuste ya está anulado");
        }
        if (ESTADO_APLICADO.equals(ajuste.getEstado())) {
            // Aplicado es definitivo: solo se revierte con un nuevo ajuste opuesto.
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Un ajuste APLICADO no puede anularse, debe registrarse un ajuste opuesto");
        }

        String motivo = request.getReason();
        String motivoActual = ajuste.getMotivo() == null ? "" : ajuste.getMotivo() + "\n";
        ajuste.setMotivo(motivoActual + "[ANULACIÓN] " + motivo);
        ajuste.setEstado(ESTADO_ANULADO);
        ajuste.setUsuario_modificacion(usuario_id);
        ajusteJpa.save(ajuste);

        return ajusteQuery.findActiveById(id, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error al recuperar el ajuste"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AjusteInventarioEntity getValidEntity(Long id, Long empresa_id) {
        AjusteInventarioEntity entity = ajusteJpa.findById(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Ajuste no encontrado"));
        if (!empresa_id.equals(entity.getEmpresa_id()) || entity.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Ajuste no encontrado");
        }
        return entity;
    }

    private List<DetalleAjusteInventarioEntity> findDetallesByAjuste(Long ajuste_id, Long empresa_id) {
        return ajusteQuery.findItemsByAjuste(ajuste_id, empresa_id).stream()
                .map(d -> detalleJpa.findById(d.getId())
                        .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Detalle de ajuste inconsistente")))
                .toList();
    }

    private void applyDetalleStockAndMovement(DetalleAjusteInventarioEntity detalle,
                                              AjusteInventarioEntity ajuste,
                                              Long empresa_id,
                                              Long usuario_id,
                                              LocalDateTime now) {
        BigDecimal diferencia = detalle.getCantidad_real().subtract(detalle.getCantidad_sistema());
        if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
            return; // Sin cambio de stock ni movimiento.
        }

        Optional<Map<String, Object>> stockOpt = ajusteQuery
                .findStockLoteInBodega(detalle.getLote_id(), ajuste.getBodega_id(), empresa_id);

        StockLoteEntity stock;
        if (stockOpt.isEmpty()) {
            // Solo permitido cuando el ajuste suma stock; restar de inexistente es imposible.
            if (diferencia.signum() < 0) {
                throw new GlobalException(HttpStatus.CONFLICT,
                        "No existe stock para el lote " + detalle.getLote_id() + " en la bodega del ajuste");
            }
            stock = new StockLoteEntity();
            stock.setEmpresa_id(empresa_id);
            stock.setSede_id(ajuste.getSede_id());
            stock.setBodega_id(ajuste.getBodega_id());
            stock.setLote_id(detalle.getLote_id());
            stock.setCantidad_disponible(diferencia);
            stock.setCantidad_reservada(BigDecimal.ZERO);
        } else {
            Long stockId = ((Number) stockOpt.get().get("stock_lote_id")).longValue();
            stock = stockJpa.findById(stockId)
                    .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Stock inconsistente"));
            BigDecimal nuevoDisponible = stock.getCantidad_disponible().add(diferencia);
            if (nuevoDisponible.signum() < 0) {
                throw new GlobalException(HttpStatus.CONFLICT,
                        "El ajuste dejaría negativo el stock del lote " + detalle.getLote_id());
            }
            stock.setCantidad_disponible(nuevoDisponible);
        }
        stock.setUltimo_movimiento_at(now);
        stockJpa.save(stock);

        boolean esPositivo = diferencia.signum() > 0;
        BigDecimal cantidadAbs = diferencia.abs();
        BigDecimal valorUnitario = detalle.getValor_unitario() != null
                ? detalle.getValor_unitario()
                : BigDecimal.ZERO;

        MovimientoInventarioEntity movimiento = new MovimientoInventarioEntity();
        movimiento.setEmpresa_id(empresa_id);
        movimiento.setSede_id(ajuste.getSede_id());
        movimiento.setTipo_movimiento(esPositivo ? TIPO_AJUSTE_POSITIVO : TIPO_AJUSTE_NEGATIVO);
        if (esPositivo) {
            movimiento.setBodega_destino_id(ajuste.getBodega_id());
        } else {
            movimiento.setBodega_origen_id(ajuste.getBodega_id());
        }
        movimiento.setLote_id(detalle.getLote_id());
        movimiento.setServicio_salud_id(detalle.getServicio_salud_id());
        movimiento.setCantidad(cantidadAbs);
        movimiento.setValor_unitario(valorUnitario);
        movimiento.setReferencia_tipo(REFERENCIA_AJUSTE);
        movimiento.setReferencia_id(ajuste.getId());
        movimiento.setMotivo("Ajuste " + ajuste.getNumero_ajuste() + " (" + ajuste.getTipo_ajuste() + ")");
        movimiento.setFecha_movimiento(now);
        movimiento.setUsuario_creacion(usuario_id);
        movimientoJpa.save(movimiento);
    }
}

package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.impactoglosa.ImpactoGlosaCarteraDto;
import com.cloud_tecnological.mednova.entity.ConcertacionGlosaEntity;
import com.cloud_tecnological.mednova.entity.CuentaPorCobrarEntity;
import com.cloud_tecnological.mednova.entity.FacturaEntity;
import com.cloud_tecnological.mednova.entity.MovimientoCuentaPorCobrarEntity;
import com.cloud_tecnological.mednova.repositories.cuentaporcobrar.CuentaPorCobrarJpaRepository;
import com.cloud_tecnological.mednova.repositories.cuentaporcobrar.CuentaPorCobrarQueryRepository;
import com.cloud_tecnological.mednova.repositories.cuentaporcobrar.MovimientoCuentaPorCobrarJpaRepository;
import com.cloud_tecnological.mednova.repositories.factura.FacturaJpaRepository;
import com.cloud_tecnological.mednova.repositories.impactoglosa.ImpactoGlosaQueryRepository;
import com.cloud_tecnological.mednova.services.ImpactoGlosaCarteraService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ImpactoGlosaCarteraServiceImpl implements ImpactoGlosaCarteraService {

    private static final String MOVIMIENTO_GLOSA_ACEPTADA = "GLOSA_ACEPTADA";
    private static final String ESTADO_PAGADA   = "PAGADA";
    private static final String ESTADO_PARCIAL  = "PARCIAL";
    private static final String ESTADO_FACTURA_PAGADA   = "PAGADA";
    private static final String ESTADO_FACTURA_APROBADA = "APROBADA";

    private final CuentaPorCobrarJpaRepository           cxcJpa;
    private final CuentaPorCobrarQueryRepository         cxcQuery;
    private final MovimientoCuentaPorCobrarJpaRepository movJpa;
    private final FacturaJpaRepository                   facturaJpa;
    private final ImpactoGlosaQueryRepository            impactoQuery;

    public ImpactoGlosaCarteraServiceImpl(CuentaPorCobrarJpaRepository cxcJpa,
                                          CuentaPorCobrarQueryRepository cxcQuery,
                                          MovimientoCuentaPorCobrarJpaRepository movJpa,
                                          FacturaJpaRepository facturaJpa,
                                          ImpactoGlosaQueryRepository impactoQuery) {
        this.cxcJpa       = cxcJpa;
        this.cxcQuery     = cxcQuery;
        this.movJpa       = movJpa;
        this.facturaJpa   = facturaJpa;
        this.impactoQuery = impactoQuery;
    }

    @Override
    @Transactional
    public ImpactoGlosaCarteraDto aplicarImpacto(ConcertacionGlosaEntity concertacion) {
        Long empresa_id = concertacion.getEmpresa_id();
        Long usuario_id = TenantContext.getUsuarioId();

        BigDecimal valorAceptadoInst = concertacion.getValor_aceptado_institucion();
        if (valorAceptadoInst == null) valorAceptadoInst = BigDecimal.ZERO;

        FacturaEntity factura = facturaJpa.findById(getFacturaIdFromGlosa(concertacion))
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        CuentaPorCobrarEntity cxc = cxcJpa.findByFacturaIdAndEmpresaId(factura.getId(), empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Cuenta por cobrar no encontrada para la factura"));

        BigDecimal saldoPrev = cxc.getSaldo_actual() == null ? BigDecimal.ZERO : cxc.getSaldo_actual();
        BigDecimal saldoNuevo = saldoPrev.subtract(valorAceptadoInst);
        if (saldoNuevo.compareTo(BigDecimal.ZERO) < 0) saldoNuevo = BigDecimal.ZERO;

        cxc.setSaldo_actual(saldoNuevo);
        cxc.setUsuario_modificacion(usuario_id);

        Long nuevoEstadoCarteraId = saldoNuevo.compareTo(BigDecimal.ZERO) == 0
                ? cxcQuery.findEstadoCarteraIdByCodigo(ESTADO_PAGADA)
                : cxcQuery.findEstadoCarteraIdByCodigo(ESTADO_PARCIAL);
        if (nuevoEstadoCarteraId != null) {
            cxc.setEstado_cartera_id(nuevoEstadoCarteraId);
        }
        cxcJpa.save(cxc);

        MovimientoCuentaPorCobrarEntity mov = new MovimientoCuentaPorCobrarEntity();
        mov.setEmpresa_id(empresa_id);
        mov.setCuenta_por_cobrar_id(cxc.getId());
        mov.setTipo_movimiento(MOVIMIENTO_GLOSA_ACEPTADA);
        mov.setValor(valorAceptadoInst);
        mov.setSaldo_resultante(saldoNuevo);
        mov.setReferencia("CONCERTACION_GLOSA:" + concertacion.getId());
        mov.setObservaciones("Aplicación automática del valor aceptado por la institución (HU-FASE2-066).");
        mov.setUsuario_creacion(usuario_id);
        mov.setFecha_movimiento(LocalDateTime.now());
        MovimientoCuentaPorCobrarEntity savedMov = movJpa.save(mov);

        boolean facturaConciliada = impactoQuery.allGlosasOfFacturaClosed(factura.getId(), empresa_id);
        String nuevoEstadoFacturaCodigo = null;
        if (facturaConciliada) {
            String codigoDestino = saldoNuevo.compareTo(BigDecimal.ZERO) == 0
                    ? ESTADO_FACTURA_PAGADA : ESTADO_FACTURA_APROBADA;
            Long nuevoEstadoFacturaId = impactoQuery.findEstadoFacturaIdByCodigo(codigoDestino);
            if (nuevoEstadoFacturaId != null) {
                factura.setEstado_factura_id(nuevoEstadoFacturaId);
                factura.setUsuario_modificacion(usuario_id);
                facturaJpa.save(factura);
                nuevoEstadoFacturaCodigo = codigoDestino;
            }
        }

        return ImpactoGlosaCarteraDto.builder()
                .glossId(concertacion.getGlosa_id())
                .concertationId(concertacion.getId())
                .invoiceId(factura.getId())
                .accountReceivableId(cxc.getId())
                .movementId(savedMov.getId())
                .movementType(MOVIMIENTO_GLOSA_ACEPTADA)
                .movementValue(valorAceptadoInst)
                .accountPreviousBalance(saldoPrev)
                .accountNewBalance(saldoNuevo)
                .movementDate(savedMov.getFecha_movimiento())
                .invoiceFullyReconciled(facturaConciliada)
                .invoiceNewStatus(nuevoEstadoFacturaCodigo)
                .build();
    }

    @Override
    public ImpactoGlosaCarteraDto consultarImpacto(Long glossId) {
        Long empresa_id = TenantContext.getEmpresaId();
        return impactoQuery.findImpactoByGlosa(glossId, empresa_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Aún no hay impacto en cartera para esta glosa"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Long getFacturaIdFromGlosa(ConcertacionGlosaEntity concertacion) {
        return impactoQuery.findFacturaIdByGlosa(concertacion.getGlosa_id(), concertacion.getEmpresa_id())
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Glosa no encontrada"));
    }
}

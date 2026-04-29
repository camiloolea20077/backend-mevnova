package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.cuentaporcobrar.CuentaPorCobrarResponseDto;
import com.cloud_tecnological.mednova.dto.cuentaporcobrar.CuentaPorCobrarTableDto;
import com.cloud_tecnological.mednova.dto.cuentaporcobrar.RegistrarAbonoRequestDto;
import com.cloud_tecnological.mednova.entity.CuentaPorCobrarEntity;
import com.cloud_tecnological.mednova.entity.FacturaEntity;
import com.cloud_tecnological.mednova.entity.MovimientoCuentaPorCobrarEntity;
import com.cloud_tecnological.mednova.repositories.cuentaporcobrar.CuentaPorCobrarJpaRepository;
import com.cloud_tecnological.mednova.repositories.cuentaporcobrar.CuentaPorCobrarQueryRepository;
import com.cloud_tecnological.mednova.repositories.cuentaporcobrar.MovimientoCuentaPorCobrarJpaRepository;
import com.cloud_tecnological.mednova.services.CuentaPorCobrarService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CuentaPorCobrarServiceImpl implements CuentaPorCobrarService {

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_PAGADA    = "PAGADA";
    private static final String ESTADO_PARCIAL   = "PARCIAL";

    private static final String MOVIMIENTO_ABONO  = "ABONO";
    private static final String MOVIMIENTO_CARGO  = "CARGO";

    private final CuentaPorCobrarJpaRepository cxcJpa;
    private final CuentaPorCobrarQueryRepository cxcQuery;
    private final MovimientoCuentaPorCobrarJpaRepository movJpa;

    public CuentaPorCobrarServiceImpl(
            CuentaPorCobrarJpaRepository cxcJpa,
            CuentaPorCobrarQueryRepository cxcQuery,
            MovimientoCuentaPorCobrarJpaRepository movJpa) {
        this.cxcJpa   = cxcJpa;
        this.cxcQuery = cxcQuery;
        this.movJpa   = movJpa;
    }

    @Override
    @Transactional
    public void crearDesdeFactura(FacturaEntity factura, Long usuarioId) {
        if (cxcJpa.findByFacturaIdAndEmpresaId(factura.getId(), factura.getEmpresa_id()).isPresent()) {
            return;
        }

        Long estadoId = cxcQuery.findEstadoCarteraIdByCodigo(ESTADO_PENDIENTE);
        if (estadoId == null) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Estado de cartera 'PENDIENTE' no configurado en el sistema");
        }

        BigDecimal total = factura.getTotal_neto() != null ? factura.getTotal_neto() : BigDecimal.ZERO;
        LocalDate hoy = LocalDate.now();

        CuentaPorCobrarEntity cxc = new CuentaPorCobrarEntity();
        cxc.setEmpresa_id(factura.getEmpresa_id());
        cxc.setSede_id(factura.getSede_id());
        cxc.setFactura_id(factura.getId());
        cxc.setPagador_id(factura.getPagador_id());
        cxc.setEstado_cartera_id(estadoId);
        cxc.setFecha_inicio(hoy);
        cxc.setFecha_causacion(hoy);
        cxc.setFecha_vencimiento(factura.getFecha_vencimiento() != null
            ? factura.getFecha_vencimiento() : hoy.plusDays(30));
        cxc.setValor_inicial(total);
        cxc.setSaldo_actual(total);
        cxc.setUsuario_creacion(usuarioId);
        cxcJpa.save(cxc);
    }

    @Override
    public CuentaPorCobrarResponseDto findById(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return cxcQuery.findActiveById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Cuenta por cobrar no encontrada"));
    }

    @Override
    public CuentaPorCobrarResponseDto findByFactura(Long facturaId) {
        Long empresaId = TenantContext.getEmpresaId();
        return cxcQuery.findByFactura(facturaId, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Cuenta por cobrar no encontrada para esta factura"));
    }

    @Override
    public PageImpl<CuentaPorCobrarTableDto> listActive(PageableDto<?> request) {
        Long empresaId = TenantContext.getEmpresaId();
        return cxcQuery.listActive(request, empresaId);
    }

    @Override
    @Transactional
    public CuentaPorCobrarResponseDto registrarAbono(Long id, RegistrarAbonoRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        CuentaPorCobrarEntity cxc = cxcJpa.findById(id)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Cuenta por cobrar no encontrada"));

        if (!cxc.getEmpresa_id().equals(empresaId) || cxc.getDeleted_at() != null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, "Cuenta por cobrar no encontrada");
        }

        BigDecimal valor = dto.getValor();
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "El valor del abono debe ser mayor a cero");
        }

        BigDecimal saldoNuevo = cxc.getSaldo_actual().subtract(valor);
        if (saldoNuevo.compareTo(BigDecimal.ZERO) < 0) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                "El abono supera el saldo actual de la cuenta por cobrar");
        }

        cxc.setSaldo_actual(saldoNuevo);
        cxc.setUsuario_modificacion(usuarioId);

        Long nuevoEstadoId;
        if (saldoNuevo.compareTo(BigDecimal.ZERO) == 0) {
            nuevoEstadoId = cxcQuery.findEstadoCarteraIdByCodigo(ESTADO_PAGADA);
        } else {
            nuevoEstadoId = cxcQuery.findEstadoCarteraIdByCodigo(ESTADO_PARCIAL);
        }
        if (nuevoEstadoId != null) {
            cxc.setEstado_cartera_id(nuevoEstadoId);
        }
        cxcJpa.save(cxc);

        MovimientoCuentaPorCobrarEntity mov = new MovimientoCuentaPorCobrarEntity();
        mov.setEmpresa_id(empresaId);
        mov.setCuenta_por_cobrar_id(cxc.getId());
        mov.setTipo_movimiento(MOVIMIENTO_ABONO);
        mov.setValor(valor);
        mov.setSaldo_resultante(saldoNuevo);
        mov.setReferencia(dto.getReferencia());
        mov.setObservaciones(dto.getObservaciones());
        mov.setUsuario_creacion(usuarioId);
        movJpa.save(mov);

        return cxcQuery.findActiveById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar CxC"));
    }
}

package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionResponseDto;
import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionSearchRequestDto;
import com.cloud_tecnological.mednova.dto.recaudo.RegistrarRecaudoRequestDto;
import com.cloud_tecnological.mednova.entity.LiquidacionCobroPacienteEntity;
import com.cloud_tecnological.mednova.repositories.liquidacion.LiquidacionCobroPacienteJpaRepository;
import com.cloud_tecnological.mednova.repositories.liquidacion.LiquidacionCobroPacienteQueryRepository;
import com.cloud_tecnological.mednova.services.LiquidacionService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LiquidacionServiceImpl implements LiquidacionService {

    private final LiquidacionCobroPacienteQueryRepository liquidacionQuery;
    private final LiquidacionCobroPacienteJpaRepository liquidacionJpa;

    public LiquidacionServiceImpl(
            LiquidacionCobroPacienteQueryRepository liquidacionQuery,
            LiquidacionCobroPacienteJpaRepository liquidacionJpa) {
        this.liquidacionQuery = liquidacionQuery;
        this.liquidacionJpa   = liquidacionJpa;
    }

    @Override
    public List<LiquidacionResponseDto> findByFactura(Long facturaId) {
        Long empresaId = TenantContext.getEmpresaId();
        return liquidacionQuery.findByFactura(facturaId, empresaId);
    }

    @Override
    public List<LiquidacionResponseDto> findByPaciente(Long pacienteId) {
        Long empresaId = TenantContext.getEmpresaId();
        return liquidacionQuery.findByPaciente(pacienteId, empresaId);
    }

    @Override
    @Transactional
    public LiquidacionResponseDto registrarRecaudo(Long id, RegistrarRecaudoRequestDto dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Long usuarioId = TenantContext.getUsuarioId();

        LiquidacionCobroPacienteEntity liq = liquidacionJpa.findActiveByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Liquidación no encontrada"));

        if ("EXENTO".equals(liq.getEstado_recaudo())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Esta liquidación está exenta de cobro");
        }
        if ("PAGADO".equals(liq.getEstado_recaudo())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, "Esta liquidación ya fue pagada");
        }

        liq.setValor_cobrado(dto.getValorCobrado());
        liq.setEstado_recaudo("PAGADO");
        if (dto.getObservaciones() != null && !dto.getObservaciones().isBlank()) {
            liq.setObservaciones(dto.getObservaciones());
        }
        liq.setUsuario_modificacion(usuarioId);
        liquidacionJpa.save(liq);

        return liquidacionQuery.findActiveById(id, empresaId)
            .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al recuperar la liquidación"));
    }

    @Override
    public PageImpl<LiquidacionResponseDto> search(LiquidacionSearchRequestDto request) {
        Long empresaId = TenantContext.getEmpresaId();
        return liquidacionQuery.search(request, empresaId);
    }
}

package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionResponseDto;
import com.cloud_tecnological.mednova.repositories.liquidacion.LiquidacionCobroPacienteQueryRepository;
import com.cloud_tecnological.mednova.services.LiquidacionService;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiquidacionServiceImpl implements LiquidacionService {

    private final LiquidacionCobroPacienteQueryRepository liquidacionQuery;

    public LiquidacionServiceImpl(LiquidacionCobroPacienteQueryRepository liquidacionQuery) {
        this.liquidacionQuery = liquidacionQuery;
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
}

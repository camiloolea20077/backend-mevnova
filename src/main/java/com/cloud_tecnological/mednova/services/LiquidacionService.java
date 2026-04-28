package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionResponseDto;

import java.util.List;

public interface LiquidacionService {
    List<LiquidacionResponseDto> findByFactura(Long facturaId);
    List<LiquidacionResponseDto> findByPaciente(Long pacienteId);
}

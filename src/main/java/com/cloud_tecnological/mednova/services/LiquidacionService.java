package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionResponseDto;
import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionSearchRequestDto;
import com.cloud_tecnological.mednova.dto.recaudo.RegistrarRecaudoRequestDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface LiquidacionService {
    List<LiquidacionResponseDto> findByFactura(Long facturaId);
    List<LiquidacionResponseDto> findByPaciente(Long pacienteId);
    LiquidacionResponseDto registrarRecaudo(Long id, RegistrarRecaudoRequestDto dto);
    PageImpl<LiquidacionResponseDto> search(LiquidacionSearchRequestDto request);
}

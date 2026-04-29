package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.rips.RipsLineaDto;
import com.cloud_tecnological.mednova.dto.rips.RipsResponseDto;

import java.util.List;

public interface RipsService {
    RipsResponseDto generarDesdeFactura(Long facturaId, String observaciones);
    RipsResponseDto findById(Long id);
    RipsResponseDto findByFactura(Long facturaId);
    List<RipsLineaDto> findLineas(Long ripsId);
}

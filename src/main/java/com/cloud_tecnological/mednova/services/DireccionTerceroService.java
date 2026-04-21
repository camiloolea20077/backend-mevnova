package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.direcciontercero.CreateDireccionTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.DireccionTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.DireccionTerceroTableDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.UpdateDireccionTerceroRequestDto;

import java.util.List;

public interface DireccionTerceroService {

    DireccionTerceroResponseDto create(CreateDireccionTerceroRequestDto request);

    DireccionTerceroResponseDto findById(Long id);

    List<DireccionTerceroTableDto> listByThirdParty(Long thirdPartyId);

    DireccionTerceroResponseDto update(Long id, UpdateDireccionTerceroRequestDto request);

    Boolean toggleActive(Long id);
}

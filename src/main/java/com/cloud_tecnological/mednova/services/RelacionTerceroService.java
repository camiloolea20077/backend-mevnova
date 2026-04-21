package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.relaciontercero.CreateRelacionTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.RelacionTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.RelacionTerceroTableDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.UpdateRelacionTerceroRequestDto;

import java.util.List;

public interface RelacionTerceroService {

    RelacionTerceroResponseDto create(CreateRelacionTerceroRequestDto request);

    RelacionTerceroResponseDto findById(Long id);

    List<RelacionTerceroTableDto> listByThirdParty(Long thirdPartyId);

    RelacionTerceroResponseDto update(Long id, UpdateRelacionTerceroRequestDto request);

    Boolean toggleActive(Long id);
}

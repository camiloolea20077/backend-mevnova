package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.centrocosto.CentroCostoResponseDto;
import com.cloud_tecnological.mednova.dto.centrocosto.CentroCostoTableDto;
import com.cloud_tecnological.mednova.dto.centrocosto.CreateCentroCostoRequestDto;
import com.cloud_tecnological.mednova.dto.centrocosto.UpdateCentroCostoRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface CentroCostoService {
    CentroCostoResponseDto create(CreateCentroCostoRequestDto request);
    CentroCostoResponseDto findById(Long id);
    CentroCostoResponseDto update(Long id, UpdateCentroCostoRequestDto request);
    Boolean toggleActive(Long id);
    PageImpl<CentroCostoTableDto> list(PageableDto<?> pageable);
}

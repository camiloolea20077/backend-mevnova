package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.bodega.BodegaResponseDto;
import com.cloud_tecnological.mednova.dto.bodega.BodegaTableDto;
import com.cloud_tecnological.mednova.dto.bodega.CreateBodegaRequestDto;
import com.cloud_tecnological.mednova.dto.bodega.UpdateBodegaRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface BodegaService {

    BodegaResponseDto create(CreateBodegaRequestDto request);

    BodegaResponseDto findById(Long id);

    PageImpl<BodegaTableDto> list(PageableDto<?> pageable);

    BodegaResponseDto update(Long id, UpdateBodegaRequestDto request);

    Boolean toggleActive(Long id);
}

package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.tercero.CreateTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.tercero.TerceroResponseDto;
import com.cloud_tecnological.mednova.dto.tercero.TerceroTableDto;
import com.cloud_tecnological.mednova.dto.tercero.UpdateTerceroRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface TerceroService {

    TerceroResponseDto create(CreateTerceroRequestDto request);

    TerceroResponseDto findById(Long id);

    TerceroResponseDto findByDocument(Long documentTypeId, String documentNumber);

    PageImpl<TerceroTableDto> listTerceros(PageableDto<?> pageable);

    TerceroResponseDto update(Long id, UpdateTerceroRequestDto request);

    Boolean toggleActive(Long id);
}

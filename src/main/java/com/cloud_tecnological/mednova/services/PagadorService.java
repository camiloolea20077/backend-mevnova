package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.pagador.CreatePagadorRequestDto;
import com.cloud_tecnological.mednova.dto.pagador.PagadorResponseDto;
import com.cloud_tecnological.mednova.dto.pagador.PagadorTableDto;
import com.cloud_tecnological.mednova.dto.pagador.UpdatePagadorRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface PagadorService {
    PagadorResponseDto create(CreatePagadorRequestDto request);
    PagadorResponseDto findById(Long id);
    PagadorResponseDto findByThirdParty(Long thirdPartyId);
    PagadorResponseDto update(Long id, UpdatePagadorRequestDto request);
    Boolean toggleActive(Long id);
    PageImpl<PagadorTableDto> list(PageableDto<?> pageable);
}

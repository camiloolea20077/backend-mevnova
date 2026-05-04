package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.lote.LoteFilterParams;
import com.cloud_tecnological.mednova.dto.lote.LoteResponseDto;
import com.cloud_tecnological.mednova.dto.lote.LoteTableDto;
import com.cloud_tecnological.mednova.dto.lote.UpdateLoteRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface LoteService {

    LoteResponseDto findById(Long id);

    PageImpl<LoteTableDto> list(PageableDto<LoteFilterParams> pageable);

    LoteResponseDto update(Long id, UpdateLoteRequestDto request);

    Boolean toggleActive(Long id);
}

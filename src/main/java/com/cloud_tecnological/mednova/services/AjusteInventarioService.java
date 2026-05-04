package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.ajuste.AjusteInventarioResponseDto;
import com.cloud_tecnological.mednova.dto.ajuste.AjusteInventarioTableDto;
import com.cloud_tecnological.mednova.dto.ajuste.CancelAjusteRequestDto;
import com.cloud_tecnological.mednova.dto.ajuste.CreateAjusteInventarioRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface AjusteInventarioService {

    AjusteInventarioResponseDto create(CreateAjusteInventarioRequestDto request);

    AjusteInventarioResponseDto findById(Long id);

    PageImpl<AjusteInventarioTableDto> list(PageableDto<?> pageable);

    AjusteInventarioResponseDto approve(Long id);

    AjusteInventarioResponseDto apply(Long id);

    AjusteInventarioResponseDto cancel(Long id, CancelAjusteRequestDto request);
}

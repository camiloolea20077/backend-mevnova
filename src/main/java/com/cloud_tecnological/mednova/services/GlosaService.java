package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.glosa.CreateGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.glosa.GlosaResponseDto;
import com.cloud_tecnological.mednova.dto.glosa.GlosaTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface GlosaService {

    GlosaResponseDto create(CreateGlosaRequestDto request);

    GlosaResponseDto findById(Long id);

    PageImpl<GlosaTableDto> list(PageableDto<?> pageable);
}

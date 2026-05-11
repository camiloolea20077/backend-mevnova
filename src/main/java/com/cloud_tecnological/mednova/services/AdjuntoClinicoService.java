package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoFilterParams;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoResponseDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoTableDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.CreateAdjuntoClinicoRequestDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.UpdateAdjuntoClinicoRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface AdjuntoClinicoService {

    AdjuntoClinicoResponseDto create(CreateAdjuntoClinicoRequestDto request);

    AdjuntoClinicoResponseDto update(Long id, UpdateAdjuntoClinicoRequestDto request);

    AdjuntoClinicoResponseDto findById(Long id);

    PageImpl<AdjuntoClinicoTableDto> list(PageableDto<AdjuntoClinicoFilterParams> pageable);

    AdjuntoClinicoResponseDto setConfidential(Long id, boolean confidential);

    void softDelete(Long id);
}

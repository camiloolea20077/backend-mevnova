package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.revision.CreateRevisionSistemasRequestDto;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasFilterParams;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasResponseDto;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasTableDto;
import com.cloud_tecnological.mednova.dto.revision.UpdateRevisionSistemasRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface RevisionSistemasService {

    RevisionSistemasResponseDto create(CreateRevisionSistemasRequestDto request);

    RevisionSistemasResponseDto update(Long id, UpdateRevisionSistemasRequestDto request);

    RevisionSistemasResponseDto findById(Long id);

    PageImpl<RevisionSistemasTableDto> list(PageableDto<RevisionSistemasFilterParams> pageable);

    RevisionSistemasResponseDto setActive(Long id, boolean active);

    void softDelete(Long id);
}

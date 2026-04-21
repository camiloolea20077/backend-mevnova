package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.sede.CreateSedeRequestDto;
import com.cloud_tecnological.mednova.dto.sede.SedeResponseDto;
import com.cloud_tecnological.mednova.dto.sede.SedeTableDto;
import com.cloud_tecnological.mednova.dto.sede.UpdateSedeRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface SedeService {

    SedeResponseDto create(CreateSedeRequestDto request);

    SedeResponseDto findById(Long id);

    PageImpl<SedeTableDto> listSedes(PageableDto<?> pageable);

    SedeResponseDto update(Long id, UpdateSedeRequestDto request);

    Boolean toggleActive(Long id);
}

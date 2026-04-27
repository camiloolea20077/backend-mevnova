package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.recursofisico.CreateRecursoFisicoRequestDto;
import com.cloud_tecnological.mednova.dto.recursofisico.RecursoFisicoResponseDto;
import com.cloud_tecnological.mednova.dto.recursofisico.RecursoFisicoTableDto;
import com.cloud_tecnological.mednova.dto.recursofisico.UpdateRecursoFisicoRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface RecursoFisicoService {
    RecursoFisicoResponseDto create(CreateRecursoFisicoRequestDto dto);
    RecursoFisicoResponseDto findById(Long id);
    RecursoFisicoResponseDto update(Long id, UpdateRecursoFisicoRequestDto dto);
    Boolean delete(Long id);
    PageImpl<RecursoFisicoTableDto> listActivos(PageableDto<?> request);
}

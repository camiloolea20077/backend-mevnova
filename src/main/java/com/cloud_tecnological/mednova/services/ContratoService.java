package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.contrato.*;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface ContratoService {
    ContratoResponseDto create(CreateContratoRequestDto request);
    ContratoResponseDto findById(Long id);
    ContratoResponseDto update(Long id, UpdateContratoRequestDto request);
    Boolean toggleActive(Long id);
    PageImpl<ContratoTableDto> list(PageableDto<?> pageable);
}

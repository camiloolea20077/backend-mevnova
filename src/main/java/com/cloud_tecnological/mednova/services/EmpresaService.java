package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.empresa.*;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface EmpresaService {

    EmpresaResponseDto create(CreateEmpresaRequestDto request);

    EmpresaResponseDto findById(Long id);

    PageImpl<EmpresaTableDto> findAll(PageableDto<?> pageable);

    EmpresaResponseDto update(Long id, UpdateEmpresaRequestDto request);

    Boolean toggleActive(Long id);
}

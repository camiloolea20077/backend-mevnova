package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarFilterParams;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarTableDto;
import com.cloud_tecnological.mednova.dto.antecedente.CreateAntecedenteFamiliarRequestDto;
import com.cloud_tecnological.mednova.dto.antecedente.UpdateAntecedenteFamiliarRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface AntecedenteFamiliarService {

    AntecedenteFamiliarResponseDto create(CreateAntecedenteFamiliarRequestDto request);

    AntecedenteFamiliarResponseDto update(Long id, UpdateAntecedenteFamiliarRequestDto request);

    AntecedenteFamiliarResponseDto findById(Long id);

    PageImpl<AntecedenteFamiliarTableDto> list(PageableDto<AntecedenteFamiliarFilterParams> pageable);

    AntecedenteFamiliarResponseDto setActive(Long id, boolean active);

    void softDelete(Long id);
}

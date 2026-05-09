package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalFilterParams;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalTableDto;
import com.cloud_tecnological.mednova.dto.antecedente.CreateAntecedentePersonalRequestDto;
import com.cloud_tecnological.mednova.dto.antecedente.TipoAntecedenteResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.UpdateAntecedentePersonalRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface AntecedentePersonalService {

    AntecedentePersonalResponseDto create(CreateAntecedentePersonalRequestDto request);

    AntecedentePersonalResponseDto update(Long id, UpdateAntecedentePersonalRequestDto request);

    AntecedentePersonalResponseDto findById(Long id);

    PageImpl<AntecedentePersonalTableDto> list(PageableDto<AntecedentePersonalFilterParams> pageable);

    AntecedentePersonalResponseDto setActive(Long id, boolean active);

    void softDelete(Long id);

    List<TipoAntecedenteResponseDto> listAntecedentTypes();
}

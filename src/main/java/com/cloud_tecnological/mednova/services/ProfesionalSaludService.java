package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.profesionalsalud.CreateProfesionalSaludRequestDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.ProfesionalSaludResponseDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.ProfesionalSaludTableDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.UpdateProfesionalSaludRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface ProfesionalSaludService {

    ProfesionalSaludResponseDto create(CreateProfesionalSaludRequestDto request);

    ProfesionalSaludResponseDto findById(Long id);

    ProfesionalSaludResponseDto findByThirdParty(Long thirdPartyId);

    PageImpl<ProfesionalSaludTableDto> listProfesionales(PageableDto<?> pageable);

    ProfesionalSaludResponseDto update(Long id, UpdateProfesionalSaludRequestDto request);

    Boolean toggleActive(Long id);
}

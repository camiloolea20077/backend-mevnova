package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.escalaclinica.CreateEscalaClinicaRequestDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaFilterParams;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaResponseDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaTableDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.UpdateEscalaClinicaRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface EscalaClinicaService {

    EscalaClinicaResponseDto create(CreateEscalaClinicaRequestDto request);

    EscalaClinicaResponseDto update(Long id, UpdateEscalaClinicaRequestDto request);

    EscalaClinicaResponseDto findById(Long id);

    PageImpl<EscalaClinicaTableDto> list(PageableDto<EscalaClinicaFilterParams> pageable);

    void softDelete(Long id);
}

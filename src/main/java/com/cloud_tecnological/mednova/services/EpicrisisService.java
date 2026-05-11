package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.epicrisis.CreateEpicrisisRequestDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisFilterParams;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisPreloadDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisResponseDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisTableDto;
import com.cloud_tecnological.mednova.dto.epicrisis.SignEpicrisisRequestDto;
import com.cloud_tecnological.mednova.dto.epicrisis.UpdateEpicrisisRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface EpicrisisService {

    EpicrisisResponseDto create(CreateEpicrisisRequestDto request);

    EpicrisisResponseDto update(Long id, UpdateEpicrisisRequestDto request);

    /** CA3: firmar bloquea edición. CA4: opcionalmente guarda pdfUrl. */
    EpicrisisResponseDto sign(Long id, SignEpicrisisRequestDto request);

    EpicrisisResponseDto findById(Long id);

    PageImpl<EpicrisisTableDto> list(PageableDto<EpicrisisFilterParams> pageable);

    /** CA2: precarga estructurada desde una admisión egresada. */
    EpicrisisPreloadDto preload(Long admissionId);

    void softDelete(Long id);
}

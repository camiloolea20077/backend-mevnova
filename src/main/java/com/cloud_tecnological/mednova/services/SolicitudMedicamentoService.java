package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.solicitudmedicamento.CancelSolicitudMedicamentoRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.CreateSolicitudMedicamentoRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DispatchSolicitudRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DispatchSuggestionItemDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.SolicitudMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.SolicitudMedicamentoTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface SolicitudMedicamentoService {

    SolicitudMedicamentoResponseDto create(CreateSolicitudMedicamentoRequestDto request);

    SolicitudMedicamentoResponseDto findById(Long id);

    PageImpl<SolicitudMedicamentoTableDto> list(PageableDto<?> pageable);

    SolicitudMedicamentoResponseDto cancel(Long id, CancelSolicitudMedicamentoRequestDto request);

    List<DispatchSuggestionItemDto> getDispatchSuggestions(Long id);

    SolicitudMedicamentoResponseDto dispatch(Long id, DispatchSolicitudRequestDto request);
}

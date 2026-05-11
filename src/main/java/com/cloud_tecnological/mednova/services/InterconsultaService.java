package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.interconsulta.CreateInterconsultaRequestDto;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaFilterParams;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaResponseDto;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaTableDto;
import com.cloud_tecnological.mednova.dto.interconsulta.RespondInterconsultaRequestDto;
import com.cloud_tecnological.mednova.dto.interconsulta.UpdateInterconsultaRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface InterconsultaService {

    /** CA1: solicitar interconsulta. */
    InterconsultaResponseDto create(CreateInterconsultaRequestDto request);

    /** Editar la solicitud (solo si no ha sido respondida). */
    InterconsultaResponseDto update(Long id, UpdateInterconsultaRequestDto request);

    /** CA3: registrar respuesta (enlaza atención de respuesta y marca RESPONDIDA). */
    InterconsultaResponseDto respond(Long id, RespondInterconsultaRequestDto request);

    /** Tomar la interconsulta (PENDIENTE → EN_PROCESO). */
    InterconsultaResponseDto markInProgress(Long id, Long respondingProfessionalId);

    /** Anular interconsulta (solo si está PENDIENTE o EN_PROCESO). */
    InterconsultaResponseDto cancel(Long id, String reason);

    InterconsultaResponseDto findById(Long id);

    /** CA2: bandeja del especialista (cross-sede, ordenada por prioridad). */
    PageImpl<InterconsultaTableDto> list(PageableDto<InterconsultaFilterParams> pageable);

    void softDelete(Long id);
}

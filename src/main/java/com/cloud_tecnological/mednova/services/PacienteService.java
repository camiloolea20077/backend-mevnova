package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.paciente.CreatePacienteRequestDto;
import com.cloud_tecnological.mednova.dto.paciente.PacienteResponseDto;
import com.cloud_tecnological.mednova.dto.paciente.PacienteTableDto;
import com.cloud_tecnological.mednova.dto.paciente.UpdatePacienteRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface PacienteService {

    PacienteResponseDto create(CreatePacienteRequestDto request);

    PacienteResponseDto findById(Long id);

    PacienteResponseDto findByThirdParty(Long thirdPartyId);

    PageImpl<PacienteTableDto> listPacientes(PageableDto<?> pageable);

    PacienteResponseDto update(Long id, UpdatePacienteRequestDto request);

    Boolean toggleActive(Long id);
}

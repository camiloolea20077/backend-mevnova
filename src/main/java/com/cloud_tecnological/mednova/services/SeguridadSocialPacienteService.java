package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.CreateSeguridadSocialPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.SeguridadSocialPacienteResponseDto;

import java.util.List;

public interface SeguridadSocialPacienteService {

    SeguridadSocialPacienteResponseDto create(Long patientId, CreateSeguridadSocialPacienteRequestDto request);

    SeguridadSocialPacienteResponseDto findById(Long patientId, Long id);

    List<SeguridadSocialPacienteResponseDto> listByPaciente(Long patientId);

    Boolean remove(Long patientId, Long id);
}

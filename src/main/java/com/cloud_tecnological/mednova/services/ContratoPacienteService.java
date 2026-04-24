package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.contratopaciente.ContratoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.contratopaciente.CreateContratoPacienteRequestDto;

import java.util.List;

public interface ContratoPacienteService {

    ContratoPacienteResponseDto create(Long patientId, CreateContratoPacienteRequestDto request);

    ContratoPacienteResponseDto findById(Long patientId, Long id);

    List<ContratoPacienteResponseDto> listByPaciente(Long patientId);

    Boolean remove(Long patientId, Long id);
}

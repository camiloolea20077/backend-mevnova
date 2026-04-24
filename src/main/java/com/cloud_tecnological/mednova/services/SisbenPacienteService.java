package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.sisbenpaciente.CreateSisbenPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.sisbenpaciente.SisbenPacienteResponseDto;

import java.util.List;

public interface SisbenPacienteService {

    SisbenPacienteResponseDto create(Long patientId, CreateSisbenPacienteRequestDto request);

    SisbenPacienteResponseDto findById(Long patientId, Long id);

    List<SisbenPacienteResponseDto> listByPaciente(Long patientId);

    Boolean remove(Long patientId, Long id);
}

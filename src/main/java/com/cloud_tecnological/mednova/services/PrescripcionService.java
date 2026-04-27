package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.prescripcion.CreatePrescripcionRequestDto;
import com.cloud_tecnological.mednova.dto.prescripcion.PrescripcionResponseDto;

import java.util.List;

public interface PrescripcionService {
    PrescripcionResponseDto create(CreatePrescripcionRequestDto dto);
    List<PrescripcionResponseDto> findByAtencionId(Long atencionId);
    Boolean delete(Long id);
}

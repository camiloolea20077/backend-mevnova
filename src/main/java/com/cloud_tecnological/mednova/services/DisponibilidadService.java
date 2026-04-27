package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.disponibilidad.DisponibilidadResponseDto;
import com.cloud_tecnological.mednova.dto.disponibilidad.GenerateAvailabilityRequestDto;
import com.cloud_tecnological.mednova.dto.disponibilidad.SearchDisponibilidadRequestDto;

import java.util.List;

public interface DisponibilidadService {
    Integer generateAvailability(GenerateAvailabilityRequestDto dto);
    List<DisponibilidadResponseDto> search(SearchDisponibilidadRequestDto dto);
}

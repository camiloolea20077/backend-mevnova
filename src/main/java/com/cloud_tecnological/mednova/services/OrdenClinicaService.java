package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.ordenclinica.CreateOrdenClinicaRequestDto;
import com.cloud_tecnological.mednova.dto.ordenclinica.OrdenClinicaResponseDto;

import java.util.List;

public interface OrdenClinicaService {
    OrdenClinicaResponseDto create(CreateOrdenClinicaRequestDto dto);
    List<OrdenClinicaResponseDto> findByAtencionId(Long atencionId);
    Boolean delete(Long id);
}

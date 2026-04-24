package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.cobertura.CoberturaVerificacionResponseDto;

public interface CoberturaVerificacionService {

    CoberturaVerificacionResponseDto verify(Long patientId, Long serviceId);
}

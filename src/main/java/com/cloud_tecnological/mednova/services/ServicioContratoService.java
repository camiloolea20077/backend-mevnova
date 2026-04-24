package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.serviciocontrato.CreateServicioContratoRequestDto;
import com.cloud_tecnological.mednova.dto.serviciocontrato.ServicioContratoResponseDto;
import com.cloud_tecnological.mednova.dto.serviciocontrato.UpdateServicioContratoRequestDto;

import java.util.List;

public interface ServicioContratoService {

    ServicioContratoResponseDto create(Long contractId, CreateServicioContratoRequestDto request);

    ServicioContratoResponseDto findById(Long contractId, Long id);

    List<ServicioContratoResponseDto> listByContrato(Long contractId);

    ServicioContratoResponseDto update(Long contractId, Long id, UpdateServicioContratoRequestDto request);

    Boolean remove(Long contractId, Long id);
}

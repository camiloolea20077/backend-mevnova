package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.tarifacontrato.CreateTarifaContratoRequestDto;
import com.cloud_tecnological.mednova.dto.tarifacontrato.TarifaContratoResponseDto;
import com.cloud_tecnological.mednova.dto.tarifacontrato.UpdateTarifaContratoRequestDto;

import java.util.List;

public interface TarifaContratoService {
    TarifaContratoResponseDto create(Long contratoId, CreateTarifaContratoRequestDto request);
    TarifaContratoResponseDto findById(Long contratoId, Long id);
    TarifaContratoResponseDto update(Long contratoId, Long id, UpdateTarifaContratoRequestDto request);
    Boolean remove(Long contratoId, Long id);
    List<TarifaContratoResponseDto> listByContrato(Long contratoId);
}

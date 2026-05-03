package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.detalleglosa.CreateDetalleGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.DetalleGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.DetalleGlosaTableDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.GlosaReconciliationDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.UpdateDetalleGlosaRequestDto;

import java.util.List;

public interface DetalleGlosaService {

    DetalleGlosaResponseDto create(CreateDetalleGlosaRequestDto request);

    DetalleGlosaResponseDto findById(Long id);

    List<DetalleGlosaTableDto> listByGlosa(Long glosaId);

    DetalleGlosaResponseDto update(Long id, UpdateDetalleGlosaRequestDto request);

    Boolean softDelete(Long id);

    GlosaReconciliationDto getReconciliation(Long glosaId);
}

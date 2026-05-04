package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.devolucion.CreateDevolucionRequestDto;
import com.cloud_tecnological.mednova.dto.devolucion.DevolucionItemResponseDto;

import java.util.List;

public interface DevolucionService {

    List<DevolucionItemResponseDto> registerReturn(Long dispensacionId, CreateDevolucionRequestDto request);
}

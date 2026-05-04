package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.traslado.CreateTrasladoRequestDto;
import com.cloud_tecnological.mednova.dto.traslado.TrasladoResponseDto;

public interface TrasladoService {

    TrasladoResponseDto transfer(CreateTrasladoRequestDto request);
}

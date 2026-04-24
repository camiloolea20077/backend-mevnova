package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.serviciosalud.CreateServicioSaludRequestDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.ServicioSaludResponseDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.ServicioSaludTableDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.UpdateServicioSaludRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface ServicioSaludService {
    ServicioSaludResponseDto create(CreateServicioSaludRequestDto request);
    ServicioSaludResponseDto findById(Long id);
    ServicioSaludResponseDto update(Long id, UpdateServicioSaludRequestDto request);
    Boolean toggleActive(Long id);
    PageImpl<ServicioSaludTableDto> list(PageableDto<?> pageable);
}

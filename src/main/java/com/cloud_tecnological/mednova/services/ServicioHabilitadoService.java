package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.serviciohabilitado.CreateServicioHabilitadoRequestDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.ServicioHabilitadoResponseDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.ServicioHabilitadoTableDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.UpdateServicioHabilitadoRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface ServicioHabilitadoService {

    ServicioHabilitadoResponseDto create(CreateServicioHabilitadoRequestDto request);

    ServicioHabilitadoResponseDto findById(Long id);

    PageImpl<ServicioHabilitadoTableDto> listServices(PageableDto<?> pageable);

    ServicioHabilitadoResponseDto update(Long id, UpdateServicioHabilitadoRequestDto request);

    Boolean toggleActive(Long id);
}

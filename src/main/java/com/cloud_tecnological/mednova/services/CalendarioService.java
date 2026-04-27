package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.calendario.AddDetalleCalendarioRequestDto;
import com.cloud_tecnological.mednova.dto.calendario.CalendarioResponseDto;
import com.cloud_tecnological.mednova.dto.calendario.CalendarioTableDto;
import com.cloud_tecnological.mednova.dto.calendario.CreateCalendarioRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface CalendarioService {
    CalendarioResponseDto create(CreateCalendarioRequestDto dto);
    CalendarioResponseDto findById(Long id);
    PageImpl<CalendarioTableDto> listActivos(PageableDto<?> request);
    CalendarioResponseDto addDetalle(Long calendarioId, AddDetalleCalendarioRequestDto dto);
    Boolean delete(Long id);
}

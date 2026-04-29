package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.radicacion.CrearRadicacionRequestDto;
import com.cloud_tecnological.mednova.dto.radicacion.RadicacionResponseDto;
import com.cloud_tecnological.mednova.dto.radicacion.RadicacionTableDto;
import com.cloud_tecnological.mednova.dto.radicacion.RegistrarRespuestaRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface RadicacionService {
    RadicacionResponseDto crear(CrearRadicacionRequestDto dto);
    RadicacionResponseDto findById(Long id);
    PageImpl<RadicacionTableDto> listActive(PageableDto<?> request);
    RadicacionResponseDto registrarRespuesta(Long id, RegistrarRespuestaRequestDto dto);
    Boolean anular(Long id);
}

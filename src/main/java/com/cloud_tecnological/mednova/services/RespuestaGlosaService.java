package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.respuestaglosa.CreateRespuestaGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.respuestaglosa.RespuestaGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.respuestaglosa.UpdateRespuestaGlosaRequestDto;

import java.util.List;

public interface RespuestaGlosaService {

    RespuestaGlosaResponseDto create(CreateRespuestaGlosaRequestDto request);

    RespuestaGlosaResponseDto findById(Long id);

    RespuestaGlosaResponseDto findByGlossDetail(Long glossDetailId);

    List<RespuestaGlosaResponseDto> listByGloss(Long glossId);

    RespuestaGlosaResponseDto update(Long id, UpdateRespuestaGlosaRequestDto request);

    Boolean softDelete(Long id);
}

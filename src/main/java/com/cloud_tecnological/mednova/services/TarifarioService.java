package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.tarifario.*;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface TarifarioService {
    TarifarioResponseDto create(CreateTarifarioRequestDto request);
    TarifarioResponseDto findById(Long id);
    TarifarioResponseDto update(Long id, UpdateTarifarioRequestDto request);
    Boolean toggleActive(Long id);
    PageImpl<TarifarioTableDto> list(PageableDto<?> pageable);

    DetalleTarifarioResponseDto upsertDetalle(Long tarifarioId, UpsertDetalleTarifarioRequestDto request);
    DetalleTarifarioResponseDto findDetalleById(Long tarifarioId, Long detalleId);
    List<DetalleTarifarioResponseDto> listDetalles(Long tarifarioId);
    Boolean removeDetalle(Long tarifarioId, Long detalleId);
}

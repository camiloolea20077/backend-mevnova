package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.listaespera.CreateWaitListRequestDto;
import com.cloud_tecnological.mednova.dto.listaespera.WaitListResponseDto;
import com.cloud_tecnological.mednova.dto.listaespera.WaitListTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface ListaEsperaCitaService {
    WaitListResponseDto create(CreateWaitListRequestDto dto);
    WaitListResponseDto findById(Long id);
    PageImpl<WaitListTableDto> listActivos(PageableDto<?> request);
    Boolean cancel(Long id);
}

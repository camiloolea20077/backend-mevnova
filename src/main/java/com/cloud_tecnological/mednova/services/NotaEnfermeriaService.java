package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.notaenfermeria.CreateNotaEnfermeriaRequestDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaFilterParams;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaResponseDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaTableDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.UpdateNotaEnfermeriaRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface NotaEnfermeriaService {

    NotaEnfermeriaResponseDto create(CreateNotaEnfermeriaRequestDto request);

    NotaEnfermeriaResponseDto update(Long id, UpdateNotaEnfermeriaRequestDto request);

    NotaEnfermeriaResponseDto sign(Long id);

    NotaEnfermeriaResponseDto findById(Long id);

    PageImpl<NotaEnfermeriaTableDto> list(PageableDto<NotaEnfermeriaFilterParams> pageable);

    NotaEnfermeriaResponseDto setActive(Long id, boolean active);

    void softDelete(Long id);
}

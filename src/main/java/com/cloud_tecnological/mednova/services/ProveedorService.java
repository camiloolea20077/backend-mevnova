package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.proveedor.CreateProveedorRequestDto;
import com.cloud_tecnological.mednova.dto.proveedor.ProveedorResponseDto;
import com.cloud_tecnological.mednova.dto.proveedor.ProveedorTableDto;
import com.cloud_tecnological.mednova.dto.proveedor.UpdateProveedorRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface ProveedorService {

    ProveedorResponseDto create(CreateProveedorRequestDto request);

    ProveedorResponseDto findById(Long id);

    PageImpl<ProveedorTableDto> list(PageableDto<?> pageable);

    ProveedorResponseDto update(Long id, UpdateProveedorRequestDto request);

    Boolean toggleActive(Long id);
}

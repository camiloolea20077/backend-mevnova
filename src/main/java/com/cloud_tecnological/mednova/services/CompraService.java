package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.compra.CancelCompraRequestDto;
import com.cloud_tecnological.mednova.dto.compra.CompraResponseDto;
import com.cloud_tecnological.mednova.dto.compra.CompraTableDto;
import com.cloud_tecnological.mednova.dto.compra.CreateCompraRequestDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface CompraService {

    CompraResponseDto create(CreateCompraRequestDto request);

    CompraResponseDto findById(Long id);

    PageImpl<CompraTableDto> list(PageableDto<?> pageable);

    CompraResponseDto receive(Long id);

    CompraResponseDto cancel(Long id, CancelCompraRequestDto request);
}

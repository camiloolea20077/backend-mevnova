package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.stock.StockFilterParams;
import com.cloud_tecnological.mednova.dto.stock.StockResponseDto;
import com.cloud_tecnological.mednova.dto.stock.StockTableDto;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface StockService {

    StockResponseDto findById(Long id);

    PageImpl<StockTableDto> list(PageableDto<StockFilterParams> pageable);
}

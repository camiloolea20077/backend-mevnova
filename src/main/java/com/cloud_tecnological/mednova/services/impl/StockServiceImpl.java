package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.stock.StockFilterParams;
import com.cloud_tecnological.mednova.dto.stock.StockResponseDto;
import com.cloud_tecnological.mednova.dto.stock.StockTableDto;
import com.cloud_tecnological.mednova.repositories.stock.StockLoteQueryRepository;
import com.cloud_tecnological.mednova.services.StockService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService {

    private final StockLoteQueryRepository queryRepository;

    public StockServiceImpl(StockLoteQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Override
    public StockResponseDto findById(Long id) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return queryRepository.findActiveById(id, empresa_id, sede_id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND, "Stock no encontrado"));
    }

    @Override
    public PageImpl<StockTableDto> list(PageableDto<StockFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        Long sede_id    = TenantContext.getSedeId();
        return queryRepository.listStock(pageable, empresa_id, sede_id);
    }
}

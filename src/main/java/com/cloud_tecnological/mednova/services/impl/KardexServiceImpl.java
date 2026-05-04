package com.cloud_tecnological.mednova.services.impl;

import com.cloud_tecnological.mednova.dto.kardex.ExpirationAlertDto;
import com.cloud_tecnological.mednova.dto.kardex.ExpirationAlertFilterParams;
import com.cloud_tecnological.mednova.dto.kardex.KardexFilterParams;
import com.cloud_tecnological.mednova.dto.kardex.KardexItemDto;
import com.cloud_tecnological.mednova.dto.kardex.LowStockAlertDto;
import com.cloud_tecnological.mednova.dto.kardex.LowStockAlertFilterParams;
import com.cloud_tecnological.mednova.repositories.kardex.KardexQueryRepository;
import com.cloud_tecnological.mednova.services.KardexService;
import com.cloud_tecnological.mednova.util.GlobalException;
import com.cloud_tecnological.mednova.util.PageableDto;
import com.cloud_tecnological.mednova.util.TenantContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class KardexServiceImpl implements KardexService {

    private final KardexQueryRepository queryRepository;

    public KardexServiceImpl(KardexQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    @Override
    public PageImpl<KardexItemDto> listKardex(PageableDto<KardexFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        KardexFilterParams filter = pageable.getParams();
        // El kardex requiere acotar por lote o por servicio para evitar consultas sin contexto
        // y resultados gigantescos sin sentido funcional.
        if (filter == null || (filter.getBatchId() == null && filter.getHealthServiceId() == null)) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Debe indicar batchId o healthServiceId para consultar el kardex");
        }
        return queryRepository.listKardex(pageable, empresa_id);
    }

    @Override
    public PageImpl<ExpirationAlertDto> listExpirationAlerts(PageableDto<ExpirationAlertFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listExpirationAlerts(pageable, empresa_id);
    }

    @Override
    public PageImpl<LowStockAlertDto> listLowStockAlerts(PageableDto<LowStockAlertFilterParams> pageable) {
        Long empresa_id = TenantContext.getEmpresaId();
        return queryRepository.listLowStockAlerts(pageable, empresa_id);
    }
}

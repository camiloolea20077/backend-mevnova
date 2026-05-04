package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.kardex.ExpirationAlertDto;
import com.cloud_tecnological.mednova.dto.kardex.ExpirationAlertFilterParams;
import com.cloud_tecnological.mednova.dto.kardex.KardexFilterParams;
import com.cloud_tecnological.mednova.dto.kardex.KardexItemDto;
import com.cloud_tecnological.mednova.dto.kardex.LowStockAlertDto;
import com.cloud_tecnological.mednova.dto.kardex.LowStockAlertFilterParams;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;

public interface KardexService {

    PageImpl<KardexItemDto> listKardex(PageableDto<KardexFilterParams> pageable);

    PageImpl<ExpirationAlertDto> listExpirationAlerts(PageableDto<ExpirationAlertFilterParams> pageable);

    PageImpl<LowStockAlertDto> listLowStockAlerts(PageableDto<LowStockAlertFilterParams> pageable);
}

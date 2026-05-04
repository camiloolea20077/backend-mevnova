package com.cloud_tecnological.mednova.dto.kardex;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class LowStockAlertDto {

    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;

    private Long warehouseId;
    private String warehouseName;
    private Long branchId;
    private String branchName;

    private BigDecimal availableQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal totalQuantity;
    private BigDecimal minimumQuantity;
}

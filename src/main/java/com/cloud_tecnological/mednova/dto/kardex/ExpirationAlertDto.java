package com.cloud_tecnological.mednova.dto.kardex;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ExpirationAlertDto {

    private Long stockId;

    private Long batchId;
    private String batchNumber;
    private LocalDate expirationDate;
    private Integer daysUntilExpiration;
    private Boolean expired;

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
}

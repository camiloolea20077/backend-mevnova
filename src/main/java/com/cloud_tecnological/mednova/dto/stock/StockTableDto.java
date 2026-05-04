package com.cloud_tecnological.mednova.dto.stock;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class StockTableDto {

    private Long id;
    private String warehouseName;
    private String branchName;
    private String healthServiceCode;
    private String healthServiceName;
    private String batchNumber;
    private LocalDate expirationDate;
    private Integer daysUntilExpiration;
    private Boolean expired;
    private BigDecimal availableQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal totalQuantity;
}

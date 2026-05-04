package com.cloud_tecnological.mednova.dto.stock;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StockResponseDto {

    private Long id;

    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;

    private Long branchId;
    private String branchName;

    private Long batchId;
    private String batchNumber;
    private LocalDate expirationDate;
    private Integer daysUntilExpiration;
    private Boolean expired;

    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;

    private BigDecimal availableQuantity;
    private BigDecimal reservedQuantity;
    private BigDecimal totalQuantity;

    private LocalDateTime lastMovementAt;
    private Boolean active;
}

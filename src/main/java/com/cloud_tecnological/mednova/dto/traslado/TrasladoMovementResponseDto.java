package com.cloud_tecnological.mednova.dto.traslado;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TrasladoMovementResponseDto {

    private Long outboundMovementId;
    private Long inboundMovementId;
    private Long batchId;
    private String batchNumber;
    private LocalDate batchExpirationDate;
    private Long healthServiceId;
    private String healthServiceName;
    private BigDecimal quantity;
    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long targetWarehouseId;
    private String targetWarehouseName;
    private LocalDateTime movementDate;
}

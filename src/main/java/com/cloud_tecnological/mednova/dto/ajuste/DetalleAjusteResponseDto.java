package com.cloud_tecnological.mednova.dto.ajuste;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class DetalleAjusteResponseDto {

    private Long id;
    private Long batchId;
    private String batchNumber;
    private LocalDate expirationDate;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private BigDecimal systemQuantity;
    private BigDecimal realQuantity;
    private BigDecimal difference;
    private BigDecimal unitValue;
    private BigDecimal differenceValue;
    private String observations;
}

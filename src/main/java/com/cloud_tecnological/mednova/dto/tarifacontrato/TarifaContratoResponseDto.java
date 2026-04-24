package com.cloud_tecnological.mednova.dto.tarifacontrato;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TarifaContratoResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long contractId;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private BigDecimal value;
    private BigDecimal discountPercentage;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean current;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

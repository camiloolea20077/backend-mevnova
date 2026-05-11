package com.cloud_tecnological.mednova.dto.balanceliquidos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class BalanceLiquidosTableDto {

    private Long id;
    private Long encounterId;
    private Long patientId;
    private String professionalName;
    private LocalDate balanceDate;
    private String shift;
    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal balance;
    private Boolean active;
}

package com.cloud_tecnological.mednova.dto.balanceliquidos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class BalanceLiquidosResponseDto {

    private Long id;

    private Long encounterId;
    private Long patientId;

    private Long professionalId;
    private String professionalName;

    private LocalDate balanceDate;
    private String shift;

    private BigDecimal totalIngresos;
    private BigDecimal totalEgresos;
    private BigDecimal balance;

    private String observations;

    private List<BalanceItemResponseDto> items;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

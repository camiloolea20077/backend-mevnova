package com.cloud_tecnological.mednova.dto.balanceliquidos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class BalanceItemResponseDto {

    private Long id;
    private Long balanceId;
    private String type;
    private String route;
    private String description;
    private BigDecimal amountMl;
    private LocalTime recordedAt;
    private Boolean active;
    private LocalDateTime createdAt;
}

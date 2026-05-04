package com.cloud_tecnological.mednova.dto.ajuste;

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
public class AjusteInventarioResponseDto {

    private Long id;
    private String adjustmentNumber;
    private Long warehouseId;
    private String warehouseName;
    private String adjustmentType;
    private LocalDate adjustmentDate;
    private String reason;
    private BigDecimal totalAdjustmentValue;
    private String state;

    private Long createdById;
    private String createdByUsername;

    private Long approvedById;
    private String approvedByUsername;
    private LocalDateTime approvedAt;

    private Boolean active;
    private LocalDateTime createdAt;

    private List<DetalleAjusteResponseDto> items;
}

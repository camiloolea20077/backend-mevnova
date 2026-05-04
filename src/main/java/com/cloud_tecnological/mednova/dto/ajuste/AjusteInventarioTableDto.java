package com.cloud_tecnological.mednova.dto.ajuste;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AjusteInventarioTableDto {

    private Long id;
    private String adjustmentNumber;
    private String warehouseName;
    private String adjustmentType;
    private LocalDate adjustmentDate;
    private String state;
    private BigDecimal totalAdjustmentValue;
    private Boolean active;
}

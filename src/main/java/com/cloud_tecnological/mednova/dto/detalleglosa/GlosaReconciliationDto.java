package com.cloud_tecnological.mednova.dto.detalleglosa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class GlosaReconciliationDto {

    private Long glossId;
    private BigDecimal glossTotalValue;
    private BigDecimal detailsSum;
    private BigDecimal difference;
    private Boolean balanced;
    private Long detailsCount;
}

package com.cloud_tecnological.mednova.dto.detalleglosa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DetalleGlosaTableDto {

    private Long id;
    private Long invoiceItemId;
    private String serviceName;
    private BigDecimal invoiceItemTotal;
    private String glossReasonCode;
    private String glossReasonName;
    private BigDecimal glossedValue;
    private Boolean active;
}

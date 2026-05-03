package com.cloud_tecnological.mednova.dto.detalleglosa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DetalleGlosaResponseDto {

    private Long id;
    private Long glossId;
    private Long invoiceItemId;
    private String serviceName;
    private BigDecimal invoiceItemTotal;
    private Long glossReasonId;
    private String glossReasonCode;
    private String glossReasonName;
    private String glossReasonGroup;
    private BigDecimal glossedValue;
    private String payerObservation;
    private Boolean active;
    private LocalDateTime createdAt;
}

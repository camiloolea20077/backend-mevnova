package com.cloud_tecnological.mednova.dto.impactoglosa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ImpactoGlosaCarteraDto {

    private Long glossId;
    private Long concertationId;
    private Long invoiceId;
    private Long accountReceivableId;
    private Long movementId;
    private String movementType;
    private BigDecimal movementValue;
    private BigDecimal accountPreviousBalance;
    private BigDecimal accountNewBalance;
    private LocalDateTime movementDate;
    private Boolean invoiceFullyReconciled;
    private String invoiceNewStatus;
}

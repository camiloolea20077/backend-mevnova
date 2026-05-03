package com.cloud_tecnological.mednova.dto.glosa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class GlosaResponseDto {

    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private String invoicePrefix;
    private Long radicationId;
    private String radicationNumber;
    private String payerName;
    private String payerOfficeNumber;
    private LocalDate officeDate;
    private LocalDate notificationDate;
    private BigDecimal totalGlossedValue;
    private BigDecimal invoiceTotalValue;
    private String officeUrl;
    private LocalDate responseDeadline;
    private String status;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

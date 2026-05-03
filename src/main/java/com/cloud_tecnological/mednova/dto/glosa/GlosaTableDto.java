package com.cloud_tecnological.mednova.dto.glosa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class GlosaTableDto {

    private Long id;
    private String invoiceNumber;
    private String payerName;
    private String payerOfficeNumber;
    private LocalDate notificationDate;
    private LocalDate responseDeadline;
    private BigDecimal totalGlossedValue;
    private String status;
    private Boolean active;
}

package com.cloud_tecnological.mednova.dto.contrato;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ContratoResponseDto {

    private Long id;
    private Long enterpriseId;
    private String number;
    private Long payerId;
    private String payerName;
    private Long paymentModalityId;
    private String paymentModalityName;
    private Long rateScheduleId;
    private String rateScheduleName;
    private String subject;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private BigDecimal contractValue;
    private BigDecimal monthlyLimit;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

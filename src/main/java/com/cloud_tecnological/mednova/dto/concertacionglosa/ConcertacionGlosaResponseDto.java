package com.cloud_tecnological.mednova.dto.concertacionglosa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ConcertacionGlosaResponseDto {

    private Long id;
    private Long glossId;
    private String glossStatus;
    private LocalDate concertationDate;
    private BigDecimal initialGlossValue;
    private BigDecimal institutionAcceptedValue;
    private BigDecimal payerAcceptedValue;
    private BigDecimal recoveredValue;
    private String actaUrl;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

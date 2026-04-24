package com.cloud_tecnological.mednova.dto.contrato;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ContratoTableDto {

    private Long id;
    private String number;
    private String payerName;
    private String paymentModalityName;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean active;
}

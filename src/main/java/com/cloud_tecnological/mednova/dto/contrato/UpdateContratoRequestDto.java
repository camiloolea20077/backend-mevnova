package com.cloud_tecnological.mednova.dto.contrato;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateContratoRequestDto {

    @NotNull(message = "La modalidad de pago es obligatoria")
    private Long paymentModalityId;

    private Long rateScheduleId;

    private String subject;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate validFrom;

    private LocalDate validUntil;

    private BigDecimal contractValue;

    private BigDecimal monthlyLimit;

    private String observations;
}

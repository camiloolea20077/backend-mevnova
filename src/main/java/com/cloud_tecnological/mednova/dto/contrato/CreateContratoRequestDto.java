package com.cloud_tecnological.mednova.dto.contrato;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateContratoRequestDto {

    @NotBlank(message = "El número de contrato es obligatorio")
    @Size(max = 50, message = "El número no puede superar 50 caracteres")
    private String number;

    @NotNull(message = "El pagador es obligatorio")
    private Long payerId;

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

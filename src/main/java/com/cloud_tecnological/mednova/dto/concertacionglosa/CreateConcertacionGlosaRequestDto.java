package com.cloud_tecnological.mednova.dto.concertacionglosa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateConcertacionGlosaRequestDto {

    @NotNull(message = "La glosa es obligatoria")
    private Long glossId;

    @NotNull(message = "La fecha de concertación es obligatoria")
    private LocalDate concertationDate;

    @NotNull(message = "El valor aceptado por la institución es obligatorio")
    @DecimalMin(value = "0.00", message = "El valor aceptado por la institución no puede ser negativo")
    private BigDecimal institutionAcceptedValue;

    @NotNull(message = "El valor aceptado por el pagador es obligatorio")
    @DecimalMin(value = "0.00", message = "El valor aceptado por el pagador no puede ser negativo")
    private BigDecimal payerAcceptedValue;

    @NotBlank(message = "El acta de conciliación es obligatoria")
    @Size(max = 500, message = "La URL del acta no puede superar 500 caracteres")
    private String actaUrl;

    private String observations;
}

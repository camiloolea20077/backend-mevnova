package com.cloud_tecnological.mednova.dto.respuestaglosa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateRespuestaGlosaRequestDto {

    @NotBlank(message = "El tipo de respuesta es obligatorio")
    @Pattern(
            regexp = "ACEPTA_TOTAL|ACEPTA_PARCIAL|NO_ACEPTA",
            message = "Tipo de respuesta inválido"
    )
    private String responseType;

    @NotNull(message = "El valor aceptado es obligatorio")
    @DecimalMin(value = "0.00", message = "El valor aceptado no puede ser negativo")
    private BigDecimal acceptedValue;

    @NotBlank(message = "La argumentación es obligatoria")
    private String argumentation;

    @Size(max = 500, message = "La URL de soporte no puede superar 500 caracteres")
    private String supportUrl;

    private Long professionalId;
}

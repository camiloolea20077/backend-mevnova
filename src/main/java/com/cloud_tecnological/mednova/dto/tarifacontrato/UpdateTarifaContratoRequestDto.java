package com.cloud_tecnological.mednova.dto.tarifacontrato;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateTarifaContratoRequestDto {

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.00", message = "El valor no puede ser negativo")
    private BigDecimal value;

    private BigDecimal discountPercentage;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate validFrom;

    private LocalDate validUntil;

    private String observations;
}

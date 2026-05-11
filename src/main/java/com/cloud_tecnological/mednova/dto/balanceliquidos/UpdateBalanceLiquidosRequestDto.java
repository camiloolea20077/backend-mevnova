package com.cloud_tecnological.mednova.dto.balanceliquidos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Actualización del encabezado del balance.
 * Los detalles (items) son inmutables: se agregan o se eliminan, no se editan.
 */
@Getter
@Setter
public class UpdateBalanceLiquidosRequestDto {

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    private LocalDate balanceDate;

    @Pattern(regexp = "MANANA|TARDE|NOCHE|DIA_24H",
            message = "shift debe ser MANANA, TARDE, NOCHE o DIA_24H")
    private String shift;

    private String observations;
}

package com.cloud_tecnological.mednova.dto.habito;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateHabitoPacienteRequestDto {

    @NotBlank(message = "habitType es obligatorio")
    @Pattern(
            regexp = "ALCOHOL|TABACO|SUSTANCIAS_PSICOACTIVAS|EJERCICIO|ALIMENTACION|SUENO|SEXUAL|OTRO",
            message = "habitType debe ser ALCOHOL, TABACO, SUSTANCIAS_PSICOACTIVAS, EJERCICIO, ALIMENTACION, SUENO, SEXUAL u OTRO"
    )
    private String habitType;

    @NotBlank(message = "description es obligatoria")
    private String description;

    @Size(max = 100)
    private String frequency;

    @Size(max = 100)
    private String quantity;

    @Size(max = 100)
    private String consumptionTime;

    private LocalDate startDate;
    private LocalDate endDate;

    @NotBlank(message = "status es obligatorio")
    @Pattern(
            regexp = "ACTIVO|EX_CONSUMIDOR|NUNCA|OCASIONAL",
            message = "status debe ser ACTIVO, EX_CONSUMIDOR, NUNCA u OCASIONAL"
    )
    private String status;

    private String observations;
}

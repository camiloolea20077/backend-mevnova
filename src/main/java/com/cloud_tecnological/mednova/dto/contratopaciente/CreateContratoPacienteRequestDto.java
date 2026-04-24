package com.cloud_tecnological.mednova.dto.contratopaciente;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateContratoPacienteRequestDto {

    @NotNull(message = "El contrato es obligatorio")
    private Long contractId;

    private String policyNumber;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate validFrom;

    private LocalDate validUntil;

    private Boolean current;

    private String observations;
}

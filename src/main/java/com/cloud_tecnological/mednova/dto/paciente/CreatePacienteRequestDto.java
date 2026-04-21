package com.cloud_tecnological.mednova.dto.paciente;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePacienteRequestDto {

    @NotNull(message = "El tercero es obligatorio")
    private Long thirdPartyId;

    private Long bloodGroupId;

    private Long rhFactorId;

    private Long disabilityId;

    private Long attentionGroupId;

    private String knownAllergies;

    private String clinicalObservations;
}

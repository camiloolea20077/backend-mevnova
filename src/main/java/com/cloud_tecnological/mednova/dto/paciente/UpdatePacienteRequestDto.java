package com.cloud_tecnological.mednova.dto.paciente;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePacienteRequestDto {

    private Long bloodGroupId;

    private Long rhFactorId;

    private Long disabilityId;

    private Long attentionGroupId;

    private String knownAllergies;

    private String clinicalObservations;
}

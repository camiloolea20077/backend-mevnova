package com.cloud_tecnological.mednova.dto.antecedente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AntecedenteFamiliarTableDto {

    private Long id;
    private Long patientId;
    private String kinship;
    private String catalogDiagnosisCode;
    private String catalogDiagnosisName;
    private String description;
    private Integer ageOfOnset;
    private Boolean isDeceased;
    private String causeOfDeath;
    private Boolean active;
}

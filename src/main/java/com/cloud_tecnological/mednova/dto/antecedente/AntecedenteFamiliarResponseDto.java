package com.cloud_tecnological.mednova.dto.antecedente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AntecedenteFamiliarResponseDto {

    private Long id;

    private Long patientId;

    private String kinship;

    private Long catalogDiagnosisId;
    private String catalogDiagnosisCode;
    private String catalogDiagnosisName;

    private String description;
    private Integer ageOfOnset;

    private Boolean isDeceased;
    private String causeOfDeath;

    private String observations;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

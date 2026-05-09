package com.cloud_tecnological.mednova.dto.antecedente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AntecedentePersonalResponseDto {

    private Long id;

    private Long patientId;

    private Long antecedentTypeId;
    private String antecedentTypeCode;
    private String antecedentTypeName;

    private Long catalogDiagnosisId;
    private String catalogDiagnosisCode;
    private String catalogDiagnosisName;

    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean isActiveCondition;
    private Boolean isAllergy;

    private String severity;
    private String observations;

    private Long registeringProfessionalId;
    private String registeringProfessionalName;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

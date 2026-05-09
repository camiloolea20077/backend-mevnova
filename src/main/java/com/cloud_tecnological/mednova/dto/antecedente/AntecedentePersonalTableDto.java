package com.cloud_tecnological.mednova.dto.antecedente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class AntecedentePersonalTableDto {

    private Long id;
    private Long patientId;
    private String antecedentTypeCode;
    private String antecedentTypeName;
    private Boolean isAllergy;
    private String catalogDiagnosisCode;
    private String catalogDiagnosisName;
    private String description;
    private String severity;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActiveCondition;
    private Boolean active;
}

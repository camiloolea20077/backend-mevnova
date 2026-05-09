package com.cloud_tecnological.mednova.dto.plancuidados;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PlanCuidadosTableDto {

    private Long id;
    private Long encounterId;
    private Long patientId;
    private String professionalName;
    private LocalDate planDate;
    private String nursingDiagnosis;
    private String status;
    private Boolean active;
}

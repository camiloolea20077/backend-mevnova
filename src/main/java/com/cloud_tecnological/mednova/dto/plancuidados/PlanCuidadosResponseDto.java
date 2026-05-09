package com.cloud_tecnological.mednova.dto.plancuidados;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PlanCuidadosResponseDto {

    private Long id;

    private Long encounterId;
    private Long patientId;

    private Long professionalId;
    private String professionalName;

    private LocalDate planDate;

    private String nursingDiagnosis;
    private String objectives;
    private String interventions;
    private String evaluation;

    private String status;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

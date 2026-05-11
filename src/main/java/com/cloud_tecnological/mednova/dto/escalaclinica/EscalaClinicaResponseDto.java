package com.cloud_tecnological.mednova.dto.escalaclinica;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EscalaClinicaResponseDto {

    private Long id;

    private Long encounterId;
    private Long patientId;

    private Long professionalId;
    private String professionalName;

    private String scaleType;
    private LocalDateTime appliedAt;

    private Integer totalScore;
    private String interpretation;
    private String risk;

    private JsonNode scaleDetail;
    private String observations;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

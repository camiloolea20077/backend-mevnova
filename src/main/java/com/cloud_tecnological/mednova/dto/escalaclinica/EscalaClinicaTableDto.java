package com.cloud_tecnological.mednova.dto.escalaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EscalaClinicaTableDto {

    private Long id;
    private Long encounterId;
    private Long patientId;
    private String professionalName;
    private String scaleType;
    private LocalDateTime appliedAt;
    private Integer totalScore;
    private String interpretation;
    private String risk;
    private Boolean active;
}

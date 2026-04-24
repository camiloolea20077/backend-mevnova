package com.cloud_tecnological.mednova.dto.sisbenpaciente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SisbenPacienteResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long patientId;
    private Long sisbenGroupId;
    private String sisbenGroupName;
    private BigDecimal score;
    private String sisbenCard;
    private LocalDate surveyDate;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean current;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

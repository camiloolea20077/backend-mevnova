package com.cloud_tecnological.mednova.dto.sisbenpaciente;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateSisbenPacienteRequestDto {

    private Long sisbenGroupId;

    private BigDecimal score;

    private String sisbenCard;

    private LocalDate surveyDate;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate validFrom;

    private LocalDate validUntil;

    private Boolean current;

    private String observations;
}

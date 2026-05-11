package com.cloud_tecnological.mednova.dto.administracionmedicamento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdministerDoseRequestDto {

    @NotNull(message = "administrationTime es obligatorio")
    private LocalDateTime administrationTime;

    @NotNull(message = "administeredDose es obligatorio")
    @PositiveOrZero(message = "administeredDose debe ser mayor o igual a 0")
    private BigDecimal administeredDose;

    private Long routeOfAdministrationId;

    private Long dispensationId;

    private Long loteId;

    private String adverseReaction;

    private String observations;
}

package com.cloud_tecnological.mednova.dto.cita;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RescheduleAppointmentRequestDto {

    @NotNull
    private Long nuevaDisponibilidadId;

    @NotNull
    private Long motivoReprogramacionId;

    private String observaciones;
}

package com.cloud_tecnological.mednova.dto.cita;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelAppointmentRequestDto {

    @NotNull
    private Long motivoCancelacionId;

    private String observaciones;
}

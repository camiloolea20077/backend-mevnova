package com.cloud_tecnological.mednova.dto.cita;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAppointmentRequestDto {

    @NotNull
    private Long disponibilidadId;

    @NotNull
    private Long pacienteId;

    @NotNull
    private Long tipoCitaId;

    private Long servicioSaludId;
    private String motivo;
    private String observaciones;
}

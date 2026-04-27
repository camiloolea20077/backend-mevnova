package com.cloud_tecnological.mednova.dto.cita;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AppointmentTableDto {
    private Long id;
    private String numeroCita;
    private String pacienteNombre;
    private String documentoNumero;
    private String profesionalNombre;
    private String especialidadNombre;
    private LocalDate fechaCita;
    private LocalTime horaCita;
    private String estadoCitaNombre;
}

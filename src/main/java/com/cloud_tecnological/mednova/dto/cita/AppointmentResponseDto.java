package com.cloud_tecnological.mednova.dto.cita;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class AppointmentResponseDto {
    private Long id;
    private String numeroCita;
    private Long disponibilidadId;
    private Long agendaId;
    private Long pacienteId;
    private String pacienteNombre;
    private String documentoNumero;
    private Long profesionalId;
    private String profesionalNombre;
    private Long especialidadId;
    private String especialidadNombre;
    private Long tipoCitaId;
    private String tipoCitaNombre;
    private Long estadoCitaId;
    private String estadoCitaCodigo;
    private String estadoCitaNombre;
    private LocalDate fechaCita;
    private LocalTime horaCita;
    private String motivo;
    private String observaciones;
    private LocalDateTime created_at;
}

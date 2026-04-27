package com.cloud_tecnological.mednova.dto.disponibilidad;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class DisponibilidadResponseDto {
    private Long id;
    private Long agendaId;
    private Long profesionalId;
    private String profesionalNombre;
    private Long especialidadId;
    private String especialidadNombre;
    private String recursoFisicoNombre;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer cuposTotales;
    private Integer cuposOcupados;
    private Integer cuposDisponibles;
    private String estadoDisponibilidad;
    private String estadoDisponibilidadNombre;
}

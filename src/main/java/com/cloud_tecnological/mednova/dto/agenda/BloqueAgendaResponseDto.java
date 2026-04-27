package com.cloud_tecnological.mednova.dto.agenda;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class BloqueAgendaResponseDto {
    private Long id;
    private Integer diaSemana;
    private String diaSemanaLabel;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer cupos;
}

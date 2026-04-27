package com.cloud_tecnological.mednova.dto.agenda;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class CreateBloqueAgendaRequestDto {

    @NotNull
    @Min(1) @Max(7)
    private Integer diaSemana;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    @Min(1)
    private Integer cupos = 1;
}

package com.cloud_tecnological.mednova.dto.listaespera;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CreateWaitListRequestDto {

    @NotNull
    private Long pacienteId;

    private Long especialidadId;
    private Long servicioSaludId;

    @Min(1) @Max(4)
    private Integer prioridad = 3;

    private LocalDate fechaPreferidaDesde;
    private LocalDate fechaPreferidaHasta;
    private String observaciones;
}

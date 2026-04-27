package com.cloud_tecnological.mednova.dto.agenda;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateAgendaRequestDto {

    @NotNull
    private Long profesionalId;

    @NotNull
    private Long especialidadId;

    private Long recursoFisicoId;

    @NotNull
    private Long calendarioId;

    @Min(5)
    private Integer duracionCitaMinutos = 20;

    @NotNull
    private LocalDate fechaVigenciaDesde;

    private LocalDate fechaVigenciaHasta;

    private String observaciones;

    @NotEmpty
    @Valid
    private List<CreateBloqueAgendaRequestDto> bloques;
}

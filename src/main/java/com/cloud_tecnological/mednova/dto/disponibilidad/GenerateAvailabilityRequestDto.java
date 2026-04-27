package com.cloud_tecnological.mednova.dto.disponibilidad;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class GenerateAvailabilityRequestDto {

    @NotNull
    private Long agendaId;

    @NotNull
    private LocalDate fechaDesde;

    @NotNull
    private LocalDate fechaHasta;
}

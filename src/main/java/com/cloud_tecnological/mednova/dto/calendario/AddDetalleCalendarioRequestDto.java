package com.cloud_tecnological.mednova.dto.calendario;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class AddDetalleCalendarioRequestDto {

    @NotNull
    private LocalDate fecha;

    private Boolean esHabil = true;
    private Boolean esFestivo = false;
    private String descripcion;
}

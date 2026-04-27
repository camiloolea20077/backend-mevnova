package com.cloud_tecnological.mednova.dto.calendario;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class DetalleCalendarioResponseDto {
    private Long id;
    private LocalDate fecha;
    private Boolean esHabil;
    private Boolean esFestivo;
    private String descripcion;
}

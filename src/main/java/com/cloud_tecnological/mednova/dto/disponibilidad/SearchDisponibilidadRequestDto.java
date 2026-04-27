package com.cloud_tecnological.mednova.dto.disponibilidad;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class SearchDisponibilidadRequestDto {
    private Long especialidadId;
    private Long profesionalId;
    private Long agendaId;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Integer page = 0;
    private Integer rows = 20;
}

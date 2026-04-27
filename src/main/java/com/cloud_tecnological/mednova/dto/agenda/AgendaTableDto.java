package com.cloud_tecnological.mednova.dto.agenda;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class AgendaTableDto {
    private Long id;
    private String profesionalNombre;
    private String especialidadNombre;
    private String calendarioNombre;
    private String estadoAgendaNombre;
    private Integer duracionCitaMinutos;
    private LocalDate fechaVigenciaDesde;
    private LocalDate fechaVigenciaHasta;
    private Boolean activo;
}

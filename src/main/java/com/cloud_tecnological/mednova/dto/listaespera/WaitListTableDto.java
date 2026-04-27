package com.cloud_tecnological.mednova.dto.listaespera;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class WaitListTableDto {
    private Long id;
    private String pacienteNombre;
    private String documentoNumero;
    private String especialidadNombre;
    private Integer prioridad;
    private String estado;
    private LocalDate fechaPreferidaDesde;
}

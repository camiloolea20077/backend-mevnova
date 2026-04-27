package com.cloud_tecnological.mednova.dto.listaespera;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class WaitListResponseDto {
    private Long id;
    private Long pacienteId;
    private String pacienteNombre;
    private String documentoNumero;
    private Long especialidadId;
    private String especialidadNombre;
    private Long servicioSaludId;
    private String servicioSaludNombre;
    private Integer prioridad;
    private LocalDate fechaPreferidaDesde;
    private LocalDate fechaPreferidaHasta;
    private String estado;
    private String observaciones;
    private LocalDateTime created_at;
}

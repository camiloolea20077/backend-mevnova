package com.cloud_tecnological.mednova.dto.agenda;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AgendaResponseDto {
    private Long id;
    private Long profesionalId;
    private String profesionalNombre;
    private Long especialidadId;
    private String especialidadNombre;
    private Long recursoFisicoId;
    private String recursoFisicoNombre;
    private Long calendarioId;
    private String calendarioNombre;
    private Long estadoAgendaId;
    private String estadoAgendaCodigo;
    private String estadoAgendaNombre;
    private Integer duracionCitaMinutos;
    private LocalDate fechaVigenciaDesde;
    private LocalDate fechaVigenciaHasta;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
    private List<BloqueAgendaResponseDto> bloques;
}

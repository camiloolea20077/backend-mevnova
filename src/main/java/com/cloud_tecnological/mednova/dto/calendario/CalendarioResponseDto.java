package com.cloud_tecnological.mednova.dto.calendario;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CalendarioResponseDto {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime created_at;
    private List<DetalleCalendarioResponseDto> detalles;
}

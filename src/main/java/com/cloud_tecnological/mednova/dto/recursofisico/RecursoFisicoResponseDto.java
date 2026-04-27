package com.cloud_tecnological.mednova.dto.recursofisico;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class RecursoFisicoResponseDto {
    private Long id;
    private String codigo;
    private String nombre;
    private String tipo_recurso;
    private String ubicacion;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}

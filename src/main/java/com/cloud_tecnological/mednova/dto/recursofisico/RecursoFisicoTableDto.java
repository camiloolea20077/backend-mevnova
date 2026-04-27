package com.cloud_tecnological.mednova.dto.recursofisico;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecursoFisicoTableDto {
    private Long id;
    private String codigo;
    private String nombre;
    private String tipo_recurso;
    private String ubicacion;
    private Boolean activo;
}

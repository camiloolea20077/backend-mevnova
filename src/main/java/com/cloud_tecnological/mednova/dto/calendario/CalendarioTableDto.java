package com.cloud_tecnological.mednova.dto.calendario;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarioTableDto {
    private Long id;
    private String codigo;
    private String nombre;
    private Boolean activo;
}

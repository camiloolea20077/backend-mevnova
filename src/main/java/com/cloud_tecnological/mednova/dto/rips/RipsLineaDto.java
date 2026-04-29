package com.cloud_tecnological.mednova.dto.rips;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RipsLineaDto {
    private Long id;
    private Long rips_encabezado_id;
    private String tipo_archivo;
    private String linea_datos;
    private LocalDateTime created_at;
}

package com.cloud_tecnological.mednova.dto.calendario;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCalendarioRequestDto {

    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    private String descripcion;
}

package com.cloud_tecnological.mednova.dto.radicacion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegistrarRespuestaRequestDto {

    @NotBlank(message = "El estado de respuesta es obligatorio")
    private String estadoCodigo;

    private LocalDate fechaRespuesta;

    private String observaciones;
}

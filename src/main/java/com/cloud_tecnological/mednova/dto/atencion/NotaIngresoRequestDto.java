package com.cloud_tecnological.mednova.dto.atencion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotaIngresoRequestDto {

    @NotBlank
    private String motivoIngreso;

    private String enfermedadActual;
    private String antecedentes;
    private String examenFisico;
    private String analisis;
    private String plan;
    private String observaciones;
}

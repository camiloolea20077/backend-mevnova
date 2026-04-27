package com.cloud_tecnological.mednova.dto.atencion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvolucionHospitalariaRequestDto {

    @NotBlank
    private String evolucion;

    private String examenFisico;
    private String analisis;
    private String plan;
    private String observaciones;
}

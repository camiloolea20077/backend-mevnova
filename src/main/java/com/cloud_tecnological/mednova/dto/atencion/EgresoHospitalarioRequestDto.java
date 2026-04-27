package com.cloud_tecnological.mednova.dto.atencion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EgresoHospitalarioRequestDto {

    @NotBlank
    private String tipoEgreso;

    private String conducta;
    private String observaciones;
}

package com.cloud_tecnological.mednova.dto.atencion;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitarHospitalizacionRequestDto {

    @NotBlank
    private String targetService;

    @NotBlank
    private String justification;

    private String observations;
}

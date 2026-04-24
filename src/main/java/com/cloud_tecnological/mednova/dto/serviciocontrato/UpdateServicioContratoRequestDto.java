package com.cloud_tecnological.mednova.dto.serviciocontrato;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateServicioContratoRequestDto {

    private Boolean requiresAuthorization;

    private Integer maxQuantity;

    private String observations;
}

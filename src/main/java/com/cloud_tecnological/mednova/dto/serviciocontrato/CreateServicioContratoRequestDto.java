package com.cloud_tecnological.mednova.dto.serviciocontrato;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateServicioContratoRequestDto {

    @NotNull(message = "El servicio de salud es obligatorio")
    private Long healthServiceId;

    private Boolean requiresAuthorization;

    private Integer maxQuantity;

    private String observations;
}

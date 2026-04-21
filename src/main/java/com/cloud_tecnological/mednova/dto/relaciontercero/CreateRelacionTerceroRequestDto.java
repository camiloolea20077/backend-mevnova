package com.cloud_tecnological.mednova.dto.relaciontercero;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRelacionTerceroRequestDto {

    @NotNull(message = "El tercero origen es obligatorio")
    private Long sourceThirdPartyId;

    @NotNull(message = "El tercero destino es obligatorio")
    private Long destinationThirdPartyId;

    @NotNull(message = "El tipo de relación es obligatorio")
    private Long relationTypeId;

    private Boolean isResponsible = false;

    private Boolean isEmergencyContact = false;

    private String observations;
}

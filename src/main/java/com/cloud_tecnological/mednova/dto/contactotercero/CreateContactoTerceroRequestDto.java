package com.cloud_tecnological.mednova.dto.contactotercero;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateContactoTerceroRequestDto {

    @NotNull(message = "El tercero es obligatorio")
    private Long thirdPartyId;

    @NotNull(message = "El tipo de contacto es obligatorio")
    private Long contactTypeId;

    @NotBlank(message = "El valor del contacto es obligatorio")
    private String value;

    private Boolean isPrincipal = false;

    private Boolean acceptsNotifications = true;
}

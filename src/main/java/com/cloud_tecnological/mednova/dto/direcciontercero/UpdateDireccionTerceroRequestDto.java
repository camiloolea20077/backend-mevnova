package com.cloud_tecnological.mednova.dto.direcciontercero;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateDireccionTerceroRequestDto {

    private Long residenceZoneId;

    @NotNull(message = "El país es obligatorio")
    private Long countryId;

    @NotNull(message = "El departamento es obligatorio")
    private Long departmentId;

    @NotNull(message = "El municipio es obligatorio")
    private Long municipalityId;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    private String neighborhood;

    private String postalCode;

    private String reference;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Boolean isPrincipal = false;
}

package com.cloud_tecnological.mednova.dto.ordenclinica;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateOrdenClinicaRequestDto {

    @NotNull
    private Long atencionId;

    @NotBlank
    private String tipoOrden;

    private String observaciones;

    @NotEmpty(message = "La orden debe tener al menos un ítem")
    @Valid
    private List<DetalleOrdenRequestDto> items;
}

package com.cloud_tecnological.mednova.dto.prescripcion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreatePrescripcionRequestDto {

    @NotNull
    private Long atencionId;

    private String observaciones;

    @NotEmpty(message = "La prescripción debe tener al menos un medicamento")
    @Valid
    private List<DetallePrescripcionRequestDto> items;
}

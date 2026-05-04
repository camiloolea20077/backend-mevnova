package com.cloud_tecnological.mednova.dto.traslado;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateTrasladoRequestDto {

    @NotNull(message = "La bodega de origen es obligatoria")
    private Long sourceWarehouseId;

    @NotNull(message = "La bodega de destino es obligatoria")
    private Long targetWarehouseId;

    @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
    private String reason;

    @NotEmpty(message = "Debe registrar al menos un ítem a trasladar")
    @Valid
    private List<TrasladoItemRequestDto> items;
}

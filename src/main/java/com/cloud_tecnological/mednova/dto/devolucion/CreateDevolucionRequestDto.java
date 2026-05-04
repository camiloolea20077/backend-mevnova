package com.cloud_tecnological.mednova.dto.devolucion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateDevolucionRequestDto {

    @NotNull(message = "La bodega de destino es obligatoria")
    private Long targetWarehouseId;

    @NotEmpty(message = "Debe registrar al menos un ítem a devolver")
    @Valid
    private List<ReturnItemRequestDto> items;
}

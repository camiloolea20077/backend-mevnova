package com.cloud_tecnological.mednova.dto.traslado;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TrasladoItemRequestDto {

    @NotNull(message = "El lote es obligatorio")
    private Long batchId;

    @NotNull(message = "La cantidad a trasladar es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad a trasladar debe ser mayor que cero")
    private BigDecimal quantity;
}

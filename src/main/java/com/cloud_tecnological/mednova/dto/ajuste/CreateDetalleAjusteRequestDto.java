package com.cloud_tecnological.mednova.dto.ajuste;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateDetalleAjusteRequestDto {

    @NotNull(message = "El lote es obligatorio")
    private Long batchId;

    @NotNull(message = "La cantidad de sistema es obligatoria")
    @DecimalMin(value = "0.000", message = "La cantidad de sistema no puede ser negativa")
    private BigDecimal systemQuantity;

    @NotNull(message = "La cantidad real es obligatoria")
    @DecimalMin(value = "0.000", message = "La cantidad real no puede ser negativa")
    private BigDecimal realQuantity;

    @NotNull(message = "El valor unitario es obligatorio")
    @DecimalMin(value = "0.00", message = "El valor unitario no puede ser negativo")
    private BigDecimal unitValue;

    @Size(max = 300, message = "Las observaciones no pueden superar 300 caracteres")
    private String observations;
}

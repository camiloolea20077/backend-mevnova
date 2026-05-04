package com.cloud_tecnological.mednova.dto.devolucion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReturnItemRequestDto {

    @NotNull(message = "El detalle de dispensación es obligatorio")
    private Long dispensationDetailId;

    @NotNull(message = "La cantidad a devolver es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad a devolver debe ser mayor que cero")
    private BigDecimal quantity;

    @NotBlank(message = "El motivo de devolución es obligatorio")
    @Size(max = 300, message = "El motivo no puede superar 300 caracteres")
    private String reason;
}

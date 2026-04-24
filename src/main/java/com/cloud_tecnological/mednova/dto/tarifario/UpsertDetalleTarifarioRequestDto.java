package com.cloud_tecnological.mednova.dto.tarifario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpsertDetalleTarifarioRequestDto {

    @NotNull(message = "El servicio es obligatorio")
    private Long healthServiceId;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor debe ser mayor a 0")
    private BigDecimal value;

    private String observations;
}

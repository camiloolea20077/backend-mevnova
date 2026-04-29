package com.cloud_tecnological.mednova.dto.cuentaporcobrar;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RegistrarAbonoRequestDto {

    @NotNull(message = "El valor del abono es obligatorio")
    private BigDecimal valor;

    private String referencia;

    private String observaciones;
}

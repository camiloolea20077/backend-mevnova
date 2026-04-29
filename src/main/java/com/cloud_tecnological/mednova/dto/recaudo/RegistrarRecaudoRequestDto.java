package com.cloud_tecnological.mednova.dto.recaudo;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RegistrarRecaudoRequestDto {

    @NotNull(message = "El valor cobrado es obligatorio")
    private BigDecimal valorCobrado;

    private String referencia;

    private String observaciones;
}

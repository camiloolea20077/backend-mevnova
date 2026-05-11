package com.cloud_tecnological.mednova.dto.balanceliquidos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
public class BalanceItemRequestDto {

    @NotBlank(message = "type es obligatorio")
    @Pattern(regexp = "INGRESO|EGRESO", message = "type debe ser INGRESO o EGRESO")
    private String type;

    @NotBlank(message = "route es obligatorio")
    @Pattern(
            regexp = "ORAL|IV|SNG|SVD|DRENAJE|VOMITO|DEPOSICION|SUDORACION|INSENSIBLES|OTRO",
            message = "route debe ser ORAL, IV, SNG, SVD, DRENAJE, VOMITO, DEPOSICION, SUDORACION, INSENSIBLES o OTRO"
    )
    private String route;

    @Size(max = 200, message = "description no puede exceder 200 caracteres")
    private String description;

    @NotNull(message = "amountMl es obligatorio")
    @PositiveOrZero(message = "amountMl debe ser mayor o igual a 0")
    private BigDecimal amountMl;

    @NotNull(message = "recordedAt es obligatorio")
    private LocalTime recordedAt;
}

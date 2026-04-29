package com.cloud_tecnological.mednova.dto.radicacion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CrearRadicacionRequestDto {

    @NotNull(message = "El ID de la factura es obligatorio")
    private Long facturaId;

    private String numeroRadicado;

    private LocalDate fechaRadicacion;

    private LocalDate fechaLimiteRespuesta;

    private String soporteUrl;

    private String observaciones;
}

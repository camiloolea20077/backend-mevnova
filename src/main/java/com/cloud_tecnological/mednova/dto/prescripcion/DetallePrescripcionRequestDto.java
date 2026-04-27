package com.cloud_tecnological.mednova.dto.prescripcion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class DetallePrescripcionRequestDto {

    @NotNull
    private Long servicioSaludId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal dosis;

    @NotBlank
    private String unidadDosis;

    @NotNull
    private Long viaAdministracionId;

    @NotNull
    private Long frecuenciaDosisId;

    private Integer duracionDias;

    private BigDecimal cantidadDespachar;

    private String indicaciones;
}

package com.cloud_tecnological.mednova.dto.ordenclinica;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class DetalleOrdenRequestDto {

    @NotNull
    private Long servicioSaludId;

    @DecimalMin("0.01")
    private BigDecimal cantidad;

    private String indicaciones;

    @Pattern(regexp = "NORMAL|URGENTE|PRIORITARIA",
             message = "Urgencia debe ser: NORMAL, URGENTE o PRIORITARIA")
    private String urgencia;
}

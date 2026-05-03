package com.cloud_tecnological.mednova.dto.detalleglosa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateDetalleGlosaRequestDto {

    @NotNull(message = "La glosa es obligatoria")
    private Long glossId;

    @NotNull(message = "El ítem de factura es obligatorio")
    private Long invoiceItemId;

    @NotNull(message = "El motivo de glosa es obligatorio")
    private Long glossReasonId;

    @NotNull(message = "El valor glosado es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor glosado debe ser mayor que cero")
    private BigDecimal glossedValue;

    private String payerObservation;
}

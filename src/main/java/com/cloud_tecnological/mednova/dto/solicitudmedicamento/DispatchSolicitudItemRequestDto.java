package com.cloud_tecnological.mednova.dto.solicitudmedicamento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DispatchSolicitudItemRequestDto {

    @NotNull(message = "El detalle de la solicitud es obligatorio")
    private Long detailId;

    /** Lote a usar. Si null, se aplica la sugerencia FEFO automáticamente. */
    private Long batchId;

    @NotNull(message = "La cantidad a despachar es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad a despachar debe ser mayor que cero")
    private BigDecimal quantityToDispatch;

    /** Justificación obligatoria si batchId difiere de la sugerencia FEFO. */
    @Size(max = 300, message = "La justificación no puede superar 300 caracteres")
    private String overrideReason;
}

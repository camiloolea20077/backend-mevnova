package com.cloud_tecnological.mednova.dto.dispensacion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateDetalleDispensacionRequestDto {

    @NotNull(message = "El detalle de prescripción es obligatorio")
    private Long prescriptionDetailId;

    /** Lote a dispensar. Si null, se aplica la sugerencia FEFO automáticamente. */
    private Long batchId;

    @NotNull(message = "La cantidad a dispensar es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad a dispensar debe ser mayor que cero")
    private BigDecimal quantity;

    /** Justificación obligatoria si batchId difiere de la sugerencia FEFO. */
    @Size(max = 300, message = "La justificación no puede superar 300 caracteres")
    private String overrideReason;

    @Size(max = 300, message = "Las observaciones no pueden superar 300 caracteres")
    private String observations;
}

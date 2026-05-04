package com.cloud_tecnological.mednova.dto.solicitudmedicamento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateDetalleSolicitudMedicamentoRequestDto {

    @NotNull(message = "El servicio de salud es obligatorio")
    private Long healthServiceId;

    @NotNull(message = "La cantidad solicitada es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad solicitada debe ser mayor que cero")
    private BigDecimal requestedQuantity;
}

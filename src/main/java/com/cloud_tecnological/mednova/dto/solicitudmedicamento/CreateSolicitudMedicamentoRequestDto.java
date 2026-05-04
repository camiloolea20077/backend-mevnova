package com.cloud_tecnological.mednova.dto.solicitudmedicamento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSolicitudMedicamentoRequestDto {

    @NotNull(message = "La bodega de origen es obligatoria")
    private Long sourceWarehouseId;

    @NotNull(message = "La bodega de destino es obligatoria")
    private Long destinationWarehouseId;

    private Long requestingProfessionalId;

    @Pattern(
            regexp = "NORMAL|URGENTE|VITAL",
            message = "Prioridad inválida (NORMAL, URGENTE, VITAL)"
    )
    private String priority;

    private String reason;

    private String observations;

    @NotEmpty(message = "Debe registrar al menos un ítem")
    @Valid
    private List<CreateDetalleSolicitudMedicamentoRequestDto> items;
}

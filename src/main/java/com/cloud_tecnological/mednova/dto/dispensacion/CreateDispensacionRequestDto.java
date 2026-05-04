package com.cloud_tecnological.mednova.dto.dispensacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateDispensacionRequestDto {

    @NotNull(message = "La prescripción es obligatoria")
    private Long prescriptionId;

    @NotNull(message = "La bodega de despacho es obligatoria")
    private Long warehouseId;

    @NotNull(message = "El profesional dispensador es obligatorio")
    private Long dispensingProfessionalId;

    /** Receptor opcional (enfermera, paciente). */
    private Long receivingProfessionalId;

    private String observations;

    @NotEmpty(message = "Debe registrar al menos un ítem a dispensar")
    @Valid
    private List<CreateDetalleDispensacionRequestDto> items;
}

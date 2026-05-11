package com.cloud_tecnological.mednova.dto.interconsulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * Actualización de la solicitud de interconsulta antes de ser respondida.
 * No se puede editar una interconsulta en estado RESPONDIDA o ANULADA.
 */
@Getter
@Setter
public class UpdateInterconsultaRequestDto {

    @NotNull(message = "destinationSpecialtyId es obligatorio")
    private Long destinationSpecialtyId;

    @NotBlank(message = "reason es obligatorio")
    private String reason;

    private String diagnosticImpression;

    private String clinicalQuestion;

    @Pattern(
            regexp = "NORMAL|URGENTE|VITAL",
            message = "priority debe ser NORMAL, URGENTE o VITAL"
    )
    private String priority;
}

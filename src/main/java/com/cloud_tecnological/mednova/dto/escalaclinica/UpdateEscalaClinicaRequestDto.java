package com.cloud_tecnological.mednova.dto.escalaclinica;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Actualización de una aplicación de escala (corrección de captura).
 * No cambia atencion/paciente/tipo (aplicaciones repetidas se crean como registros nuevos).
 */
@Getter
@Setter
public class UpdateEscalaClinicaRequestDto {

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    @NotNull(message = "totalScore es obligatorio")
    private Integer totalScore;

    @Size(max = 200, message = "interpretation no puede exceder 200 caracteres")
    private String interpretation;

    @Pattern(
            regexp = "BAJO|MEDIO|ALTO|MUY_ALTO",
            message = "risk debe ser BAJO, MEDIO, ALTO o MUY_ALTO"
    )
    private String risk;

    private JsonNode scaleDetail;

    private String observations;
}

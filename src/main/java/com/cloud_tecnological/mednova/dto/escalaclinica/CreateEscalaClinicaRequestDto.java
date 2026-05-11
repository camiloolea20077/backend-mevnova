package com.cloud_tecnological.mednova.dto.escalaclinica;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateEscalaClinicaRequestDto {

    @NotNull(message = "encounterId es obligatorio")
    private Long encounterId;

    @NotNull(message = "patientId es obligatorio")
    private Long patientId;

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    @NotBlank(message = "scaleType es obligatorio")
    @Pattern(
            regexp = "GLASGOW|EVA|NORTON|BRADEN|MORSE|DOWNTON|BARTHEL|LAWTON|KATZ|MINI_MENTAL|APGAR|SILVERMAN|GLASGOW_PEDIATRICO|OTRA",
            message = "scaleType debe ser una escala válida"
    )
    private String scaleType;

    private LocalDateTime appliedAt;

    @NotNull(message = "totalScore es obligatorio")
    private Integer totalScore;

    @Size(max = 200, message = "interpretation no puede exceder 200 caracteres")
    private String interpretation;

    @Pattern(
            regexp = "BAJO|MEDIO|ALTO|MUY_ALTO",
            message = "risk debe ser BAJO, MEDIO, ALTO o MUY_ALTO"
    )
    private String risk;

    /** Estructura JSON con los ítems específicos de la escala. */
    private JsonNode scaleDetail;

    private String observations;
}

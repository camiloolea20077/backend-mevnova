package com.cloud_tecnological.mednova.dto.interconsulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RespondInterconsultaRequestDto {

    @NotNull(message = "respondingProfessionalId es obligatorio")
    private Long respondingProfessionalId;

    @NotBlank(message = "response es obligatorio")
    private String response;

    private String recommendations;

    /** Atención generada por el especialista al atender al paciente (opcional). */
    private Long responseEncounterId;
}

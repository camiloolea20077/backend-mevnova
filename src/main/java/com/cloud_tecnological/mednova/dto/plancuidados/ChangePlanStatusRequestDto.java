package com.cloud_tecnological.mednova.dto.plancuidados;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePlanStatusRequestDto {

    @NotBlank(message = "status es obligatorio")
    @Pattern(
            regexp = "ACTIVO|CUMPLIDO|MODIFICADO|SUSPENDIDO",
            message = "status debe ser ACTIVO, CUMPLIDO, MODIFICADO o SUSPENDIDO"
    )
    private String status;

    /** Nota de evaluación opcional al cambiar de estado (ej: motivo de suspensión, cumplimiento). */
    private String evaluation;
}

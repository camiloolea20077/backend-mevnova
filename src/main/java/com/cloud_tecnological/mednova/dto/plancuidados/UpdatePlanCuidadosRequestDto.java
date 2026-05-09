package com.cloud_tecnological.mednova.dto.plancuidados;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdatePlanCuidadosRequestDto {

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    private LocalDate planDate;

    @NotBlank(message = "nursingDiagnosis es obligatorio")
    private String nursingDiagnosis;

    @NotBlank(message = "objectives es obligatorio")
    private String objectives;

    @NotBlank(message = "interventions es obligatorio")
    private String interventions;

    private String evaluation;

    @Pattern(
            regexp = "ACTIVO|CUMPLIDO|MODIFICADO|SUSPENDIDO",
            message = "status debe ser ACTIVO, CUMPLIDO, MODIFICADO o SUSPENDIDO"
    )
    private String status;
}

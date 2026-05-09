package com.cloud_tecnological.mednova.dto.antecedente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateAntecedentePersonalRequestDto {

    @NotNull(message = "antecedentTypeId es obligatorio")
    private Long antecedentTypeId;

    private Long catalogDiagnosisId;

    @NotBlank(message = "description es obligatoria")
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @Pattern(regexp = "LEVE|MODERADA|GRAVE|CRITICA",
            message = "severity debe ser LEVE, MODERADA, GRAVE o CRITICA")
    @Size(max = 20)
    private String severity;

    private String observations;

    private Long registeringProfessionalId;

    private Boolean isActiveCondition;
}

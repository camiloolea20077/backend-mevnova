package com.cloud_tecnological.mednova.dto.antecedente;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAntecedenteFamiliarRequestDto {

    @NotNull(message = "patientId es obligatorio")
    private Long patientId;

    @NotBlank(message = "kinship es obligatorio")
    @Size(max = 50, message = "kinship no puede superar 50 caracteres")
    private String kinship;

    private Long catalogDiagnosisId;

    @NotBlank(message = "description es obligatoria")
    private String description;

    @Min(value = 0, message = "ageOfOnset debe ser mayor o igual a 0")
    private Integer ageOfOnset;

    private Boolean isDeceased;

    @Size(max = 300, message = "causeOfDeath no puede superar 300 caracteres")
    private String causeOfDeath;

    private String observations;
}

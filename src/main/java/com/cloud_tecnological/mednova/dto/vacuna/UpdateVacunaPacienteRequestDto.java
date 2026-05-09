package com.cloud_tecnological.mednova.dto.vacuna;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateVacunaPacienteRequestDto {

    @NotBlank(message = "vaccineName es obligatorio")
    @Size(max = 150)
    private String vaccineName;

    @Size(max = 30)
    private String vaccineCode;

    @NotNull(message = "doseNumber es obligatorio")
    @Min(value = 1, message = "doseNumber debe ser mayor o igual a 1")
    private Integer doseNumber;

    @Min(value = 1, message = "totalSchemeDoses debe ser mayor o igual a 1")
    private Integer totalSchemeDoses;

    @NotNull(message = "applicationDate es obligatoria")
    private LocalDate applicationDate;

    private LocalDate nextDoseDate;

    @Size(max = 100)
    private String laboratory;

    @Size(max = 50)
    private String batchNumber;

    private Long administrationRouteId;
    private Long applyingProfessionalId;

    @Size(max = 200)
    private String applyingInstitution;

    private String observations;
}

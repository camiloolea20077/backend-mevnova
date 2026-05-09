package com.cloud_tecnological.mednova.dto.medicacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateMedicacionHabitualRequestDto {

    private Long healthServiceId;

    @NotBlank(message = "medicationName es obligatorio")
    @Size(max = 200)
    private String medicationName;

    @Size(max = 50)
    private String dose;

    private Long administrationRouteId;
    private Long doseFrequencyId;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 300)
    private String indication;

    @Size(max = 200)
    private String prescribingProfessional;

    private Boolean isCurrentlyTaking;

    private String observations;
}

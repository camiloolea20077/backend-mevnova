package com.cloud_tecnological.mednova.dto.profesionalsalud;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateProfesionalSaludRequestDto {

    @NotNull(message = "El tercero es obligatorio")
    private Long thirdPartyId;

    private String medicalRegistrationNumber;

    @NotNull(message = "La especialidad principal es obligatoria")
    private Long primarySpecialtyId;

    private LocalDate startDate;

    private String observations;
}

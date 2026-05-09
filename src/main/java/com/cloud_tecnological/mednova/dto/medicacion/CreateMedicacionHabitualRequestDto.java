package com.cloud_tecnological.mednova.dto.medicacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateMedicacionHabitualRequestDto {

    @NotNull(message = "patientId es obligatorio")
    private Long patientId;

    /** Referencia al catálogo de servicios de salud (opcional, si está homologado). */
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

    /** Profesional prescriptor en texto libre (puede ser externo). */
    @Size(max = 200)
    private String prescribingProfessional;

    /** true = el paciente aún toma el medicamento. */
    private Boolean isCurrentlyTaking;

    private String observations;
}

package com.cloud_tecnological.mednova.dto.medicacion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MedicacionHabitualResponseDto {

    private Long id;

    private Long patientId;

    private Long healthServiceId;
    private String healthServiceName;

    private String medicationName;
    private String dose;

    private Long administrationRouteId;
    private String administrationRouteName;

    private Long doseFrequencyId;
    private String doseFrequencyName;

    private LocalDate startDate;
    private LocalDate endDate;

    private String indication;
    private String prescribingProfessional;

    private Boolean isCurrentlyTaking;
    private String observations;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

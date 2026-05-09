package com.cloud_tecnological.mednova.dto.vacuna;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class VacunaPacienteResponseDto {

    private Long id;

    private Long patientId;

    private String vaccineName;
    private String vaccineCode;

    private Integer doseNumber;
    private Integer totalSchemeDoses;

    private LocalDate applicationDate;
    private LocalDate nextDoseDate;

    private String laboratory;
    private String batchNumber;

    private Long administrationRouteId;
    private String administrationRouteName;

    private Long applyingProfessionalId;
    private String applyingProfessionalName;

    private String applyingInstitution;
    private String observations;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

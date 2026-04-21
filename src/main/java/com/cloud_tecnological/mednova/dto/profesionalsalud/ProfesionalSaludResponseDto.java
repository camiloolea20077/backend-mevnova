package com.cloud_tecnological.mednova.dto.profesionalsalud;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProfesionalSaludResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long thirdPartyId;
    private String fullName;
    private String documentNumber;
    private String documentTypeCode;
    private String medicalRegistrationNumber;
    private Long primarySpecialtyId;
    private String primarySpecialtyName;
    private LocalDate startDate;
    private LocalDate retirementDate;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

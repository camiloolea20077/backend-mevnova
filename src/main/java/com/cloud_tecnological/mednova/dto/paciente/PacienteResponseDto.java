package com.cloud_tecnological.mednova.dto.paciente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PacienteResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long thirdPartyId;
    private String fullName;
    private String documentNumber;
    private String documentTypeCode;
    private LocalDate birthDate;
    private Long bloodGroupId;
    private String bloodGroupName;
    private Long rhFactorId;
    private String rhFactorName;
    private Long disabilityId;
    private Long attentionGroupId;
    private String knownAllergies;
    private String clinicalObservations;
    private Boolean active;
    private LocalDateTime createdAt;
}

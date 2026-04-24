package com.cloud_tecnological.mednova.dto.contratopaciente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ContratoPacienteResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long patientId;
    private Long contractId;
    private String contractNumber;
    private String payerName;
    private String policyNumber;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean current;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

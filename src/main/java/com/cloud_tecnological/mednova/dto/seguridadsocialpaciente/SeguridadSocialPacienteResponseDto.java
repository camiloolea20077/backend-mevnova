package com.cloud_tecnological.mednova.dto.seguridadsocialpaciente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SeguridadSocialPacienteResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long patientId;
    private Long payerId;
    private String payerName;
    private Long regimenId;
    private String regimenName;
    private Long affiliationCategoryId;
    private Long affiliationTypeId;
    private String affiliationNumber;
    private Long cotizanteThirdPartyId;
    private String cotizanteFullName;
    private LocalDate affiliationDate;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean current;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

package com.cloud_tecnological.mednova.dto.interconsulta;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InterconsultaResponseDto {

    private Long id;
    private String number;

    private Long originEncounterId;
    private Long responseEncounterId;

    private Long requestingProfessionalId;
    private String requestingProfessionalName;

    private Long respondingProfessionalId;
    private String respondingProfessionalName;

    private Long destinationSpecialtyId;
    private String destinationSpecialtyName;

    private String reason;
    private String diagnosticImpression;
    private String clinicalQuestion;

    private String status;
    private String priority;

    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private String response;
    private String recommendations;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

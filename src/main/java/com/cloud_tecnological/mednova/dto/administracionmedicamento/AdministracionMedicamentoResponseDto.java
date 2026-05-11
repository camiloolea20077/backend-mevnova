package com.cloud_tecnological.mednova.dto.administracionmedicamento;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdministracionMedicamentoResponseDto {

    private Long id;

    private Long encounterId;
    private Long patientId;

    private Long prescriptionDetailId;
    private String medicationName;

    private Long dispensationId;
    private String dispensationNumber;

    private Long loteId;
    private String loteNumber;

    private Long professionalId;
    private String professionalName;

    private LocalDateTime scheduledAt;
    private LocalDateTime administeredAt;

    private BigDecimal administeredDose;

    private Long routeOfAdministrationId;
    private String routeOfAdministrationName;

    private String status;

    private String omissionReason;
    private String adverseReaction;
    private String observations;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

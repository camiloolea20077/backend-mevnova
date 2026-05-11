package com.cloud_tecnological.mednova.dto.administracionmedicamento;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdministracionMedicamentoTableDto {

    private Long id;
    private Long encounterId;
    private Long patientId;
    private Long prescriptionDetailId;
    private String medicationName;
    private String professionalName;
    private LocalDateTime scheduledAt;
    private LocalDateTime administeredAt;
    private BigDecimal administeredDose;
    private String status;
    private Boolean hasAdverseReaction;
    private Boolean active;
}

package com.cloud_tecnological.mednova.dto.admision;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdmisionRequestDto {

    @NotNull(message = "El paciente es obligatorio")
    private Long patientId;

    @NotNull(message = "El tipo de admisión es obligatorio")
    private Long admissionTypeId;

    @NotNull(message = "El origen de atención es obligatorio")
    private Long careOriginId;

    @NotNull(message = "El pagador es obligatorio")
    private Long payerId;

    private Long contractId;

    private Long companionId;

    private String entryReason;

    private String observations;
}

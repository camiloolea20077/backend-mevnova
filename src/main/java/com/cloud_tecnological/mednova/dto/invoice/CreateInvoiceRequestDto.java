package com.cloud_tecnological.mednova.dto.invoice;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInvoiceRequestDto {

    @NotNull(message = "La admisión es obligatoria")
    private Long admissionId;

    private String prefix;

    private String observations;
}

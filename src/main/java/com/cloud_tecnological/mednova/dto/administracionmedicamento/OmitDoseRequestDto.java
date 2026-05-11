package com.cloud_tecnological.mednova.dto.administracionmedicamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OmitDoseRequestDto {

    /** Estado final: OMITIDA o RECHAZADA. */
    @NotBlank(message = "status es obligatorio")
    @Pattern(
            regexp = "OMITIDA|RECHAZADA",
            message = "status debe ser OMITIDA o RECHAZADA"
    )
    private String status;

    @NotBlank(message = "omissionReason es obligatorio")
    @Size(max = 300, message = "omissionReason no puede exceder 300 caracteres")
    private String omissionReason;

    private String observations;
}

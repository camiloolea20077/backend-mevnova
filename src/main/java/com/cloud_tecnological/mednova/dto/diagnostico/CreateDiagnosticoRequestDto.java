package com.cloud_tecnological.mednova.dto.diagnostico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDiagnosticoRequestDto {

    @NotNull
    private Long atencionId;

    @NotNull
    private Long catalogoDiagnosticoId;

    @NotBlank
    @Pattern(regexp = "PRINCIPAL|RELACIONADO|IMPRESION|EGRESO",
             message = "Tipo debe ser: PRINCIPAL, RELACIONADO, IMPRESION o EGRESO")
    private String tipoDiagnostico;

    private Boolean esConfirmado;

    private String observaciones;
}

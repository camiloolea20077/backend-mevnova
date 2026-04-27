package com.cloud_tecnological.mednova.dto.atencion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CerrarAtencionRequestDto {

    @NotBlank
    @Pattern(regexp = "ALTA|OBSERVACION|REMISION",
             message = "Conducta debe ser: ALTA, OBSERVACION o REMISION")
    private String conduct;

    private String plan;
    private String analysis;
    private String observations;
}

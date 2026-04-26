package com.cloud_tecnological.mednova.dto.admision;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EgresoAdmisionRequestDto {

    @NotBlank(message = "El tipo de egreso es obligatorio")
    private String dischargeType;

    private String observations;
}

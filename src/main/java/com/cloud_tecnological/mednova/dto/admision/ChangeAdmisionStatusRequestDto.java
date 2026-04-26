package com.cloud_tecnological.mednova.dto.admision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeAdmisionStatusRequestDto {

    @NotNull(message = "El nuevo estado es obligatorio")
    @NotBlank(message = "El nuevo estado es obligatorio")
    private String newStatusCode;

    private String observations;
}

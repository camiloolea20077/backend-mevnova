package com.cloud_tecnological.mednova.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreAuthRequestDto {

    @Size(min = 3, max = 20, message = "El código de empresa debe tener entre 3 y 20 caracteres")
    private String companyCodigo;

    @Size(min = 3, max = 20, message = "El NIT debe tener entre 3 y 20 caracteres")
    private String companyNit;
}

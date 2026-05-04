package com.cloud_tecnological.mednova.dto.lote;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLoteRequestDto {

    @Size(max = 50, message = "El registro INVIMA no puede superar 50 caracteres")
    private String invimaRegister;

    private String observations;
}

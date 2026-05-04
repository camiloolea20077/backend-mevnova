package com.cloud_tecnological.mednova.dto.compra;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelCompraRequestDto {

    @NotBlank(message = "La justificación es obligatoria")
    @Size(max = 500, message = "La justificación no puede superar 500 caracteres")
    private String reason;
}

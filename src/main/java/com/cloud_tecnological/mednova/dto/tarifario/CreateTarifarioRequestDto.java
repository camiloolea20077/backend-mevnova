package com.cloud_tecnological.mednova.dto.tarifario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateTarifarioRequestDto {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 30, message = "El código no puede superar 30 caracteres")
    private String code;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String name;

    private String description;

    @NotNull(message = "La fecha de vigencia desde es obligatoria")
    private LocalDate validFrom;

    private LocalDate validUntil;
}

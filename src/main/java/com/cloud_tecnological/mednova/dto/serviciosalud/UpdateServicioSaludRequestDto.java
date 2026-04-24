package com.cloud_tecnological.mednova.dto.serviciosalud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateServicioSaludRequestDto {

    @Size(max = 20, message = "El código CUPS no puede superar 20 caracteres")
    private String cupsCode;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 300, message = "El nombre no puede superar 300 caracteres")
    private String name;

    private String description;

    @NotNull(message = "La categoría es obligatoria")
    private Long healthServiceCategoryId;

    private Long costCenterId;

    @Size(max = 30, message = "La unidad de medida no puede superar 30 caracteres")
    private String measureUnit;

    private Boolean requiresAuthorization;

    private Boolean requiresDiagnosis;
}

package com.cloud_tecnological.mednova.dto.centrocosto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCentroCostoRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String name;

    private Long parentId;

    private String description;
}

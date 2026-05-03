package com.cloud_tecnological.mednova.dto.bodega;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBodegaRequestDto {

    @NotNull(message = "La sede es obligatoria")
    private Long branchId;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20, message = "El código no puede superar 20 caracteres")
    private String code;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String name;

    @NotBlank(message = "El tipo de bodega es obligatorio")
    @Pattern(
            regexp = "FARMACIA_CENTRAL|BOTIQUIN_PISO|QUIROFANO|URGENCIAS|CONSULTORIO|CARRO_PARO|OTRA",
            message = "Tipo de bodega inválido"
    )
    private String warehouseType;

    private Long responsibleId;

    @Size(max = 200, message = "La ubicación física no puede superar 200 caracteres")
    private String physicalLocation;

    private Boolean isPrincipal;

    private Boolean allowsDispense;

    private Boolean allowsReceive;

    private String observations;
}

package com.cloud_tecnological.mednova.dto.atencion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignBedRequestDto {

    @NotNull
    private Long recursoFisicoId;

    private Long profesionalId;
    private String observaciones;
}

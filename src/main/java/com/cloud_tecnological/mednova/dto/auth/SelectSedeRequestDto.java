package com.cloud_tecnological.mednova.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelectSedeRequestDto {

    @NotNull(message = "El ID de sede es obligatorio")
    private Long sedeId;
}

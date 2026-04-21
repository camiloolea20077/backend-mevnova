package com.cloud_tecnological.mednova.dto.usuariorol;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AsignarRolRequestDto {

    @NotNull(message = "El usuario es obligatorio")
    private Long userId;

    @NotNull(message = "El rol es obligatorio")
    private Long roleId;

    private Long sedeId;

    private LocalDate validFrom;

    private LocalDate validUntil;
}

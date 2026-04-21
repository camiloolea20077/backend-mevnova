package com.cloud_tecnological.mednova.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwitchTenantRequestDto {

    @NotNull(message = "La empresa es obligatoria")
    private Long empresaId;

    private Long sedeId;
}
package com.cloud_tecnological.mednova.dto.usuariorol;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UsuarioRolTableDto {

    private Long id;
    private String username;
    private String roleName;
    private String sedeName;
    private Boolean active;
    private LocalDate validFrom;
    private LocalDate validUntil;
}

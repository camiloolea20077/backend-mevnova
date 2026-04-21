package com.cloud_tecnological.mednova.dto.usuariorol;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UsuarioRolResponseDto {

    private Long id;
    private Long userId;
    private String username;
    private Long roleId;
    private String roleName;
    private Long sedeId;
    private String sedeName;
    private Boolean active;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private LocalDateTime createdAt;
}

package com.cloud_tecnological.mednova.dto.usuario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UsuarioResponseDto {

    private Long id;
    private String username;
    private String email;
    private Long thirdPartyId;
    private Boolean active;
    private Boolean blocked;
    private Boolean requiresPasswordChange;
    private Integer failedAttempts;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}

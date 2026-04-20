package com.cloud_tecnological.mednova.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class LoginResponseDto {

    // Caso: varias sedes disponibles
    private String sessionToken;
    private Boolean requiresSedeSelection;
    private List<SedeDto> availableSedes;
    private Long expiresInSeconds;

    // Caso: una sola sede → JWT final directo
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserInfoDto user;

    // Caso: requiere cambio de contraseña
    private Boolean requirePasswordChange;
    private String passwordChangeToken;
}

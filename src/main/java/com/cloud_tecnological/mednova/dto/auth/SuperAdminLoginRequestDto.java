package com.cloud_tecnological.mednova.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperAdminLoginRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}

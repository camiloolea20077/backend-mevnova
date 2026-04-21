package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.auth.*;

public interface AuthService {

    PreAuthResponseDto preAuth(PreAuthRequestDto request, String ip, String userAgent);

    LoginResponseDto login(String preAuthToken, LoginRequestDto request, String ip, String userAgent);

    AuthTokensDto selectSede(String sessionToken, SelectSedeRequestDto request, String ip, String userAgent);

    void logout(String accessToken, String ip, String userAgent);

    AuthTokensDto superAdminLogin(SuperAdminLoginRequestDto request, String ip, String userAgent);

    AuthTokensDto refresh(String refreshToken);

    void changePassword(String token, ChangePasswordRequestDto request, String ip, String userAgent);

    AuthTokensDto switchTenant(SwitchTenantRequestDto request, String ip, String userAgent);
}

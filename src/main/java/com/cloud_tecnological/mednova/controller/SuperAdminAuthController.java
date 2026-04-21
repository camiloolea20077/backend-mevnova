package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.auth.AuthTokensDto;
import com.cloud_tecnological.mednova.dto.auth.SuperAdminLoginRequestDto;
import com.cloud_tecnological.mednova.dto.auth.SwitchTenantRequestDto;
import com.cloud_tecnological.mednova.services.AuthService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/super-admin")
public class SuperAdminAuthController {

    private final AuthService authService;

    public SuperAdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokensDto>> login(
            @Valid @RequestBody SuperAdminLoginRequestDto request,
            HttpServletRequest httpRequest) {
        AuthTokensDto result = authService.superAdminLogin(
                request,
                extractClientIp(httpRequest),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), "Login exitoso", false, result));
    }

    @PostMapping("/switch-tenant")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AuthTokensDto>> switchTenant(
            @Valid @RequestBody SwitchTenantRequestDto request,
            HttpServletRequest httpRequest) {
        AuthTokensDto result = authService.switchTenant(
                request,
                extractClientIp(httpRequest),
                httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), "Tenant cambiado", false, result));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.auth.*;
import com.cloud_tecnological.mednova.services.AuthService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/pre-auth")
    public ResponseEntity<ApiResponse<PreAuthResponseDto>> preAuth(
            @Valid @RequestBody PreAuthRequestDto request,
            HttpServletRequest httpRequest) {
        PreAuthResponseDto result = authService.preAuth(
                request, extractClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), "Empresa validada", false, result));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @RequestHeader("X-Pre-Auth-Token") String preAuthToken,
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {
        LoginResponseDto result = authService.login(
                preAuthToken, request,
                extractClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), "Login exitoso", false, result));
    }

    @PostMapping("/select-sede")
    public ResponseEntity<ApiResponse<AuthTokensDto>> selectSede(
            @RequestHeader("X-Session-Token") String sessionToken,
            @Valid @RequestBody SelectSedeRequestDto request,
            HttpServletRequest httpRequest) {
        AuthTokensDto result = authService.selectSede(
                sessionToken, request,
                extractClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), "Sede seleccionada", false, result));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(
            @RequestHeader("Authorization") String bearer,
            HttpServletRequest httpRequest) {
        // Extraer el token sin el prefijo "Bearer "
        String token = bearer.startsWith("Bearer ") ? bearer.substring(7) : bearer;
        authService.logout(token, extractClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(), "Logout exitoso", false, true));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

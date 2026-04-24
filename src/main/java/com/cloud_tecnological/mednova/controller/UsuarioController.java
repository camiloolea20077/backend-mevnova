package com.cloud_tecnological.mednova.controller;

import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_tecnological.mednova.dto.usuario.CreateUsuarioRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.ResetPasswordRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.UpdateUsuarioRequestDto;
import com.cloud_tecnological.mednova.dto.usuario.UsuarioResponseDto;
import com.cloud_tecnological.mednova.dto.usuario.UsuarioTableDto;
import com.cloud_tecnological.mednova.services.UsuarioService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")

public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> create(
            @Valid @RequestBody CreateUsuarioRequestDto request) {
        UsuarioResponseDto result = usuarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Usuario creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> findById(@PathVariable Long id) {
        UsuarioResponseDto result = usuarioService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<UsuarioTableDto>>> listUsuarios(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<UsuarioTableDto> result = usuarioService.listUsuarios(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioRequestDto request) {
        UsuarioResponseDto result = usuarioService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Usuario actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = usuarioService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Usuario activado" : "Usuario inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }

    @PatchMapping("/{id}/toggle-blocked")
    public ResponseEntity<ApiResponse<Boolean>> toggleBlocked(@PathVariable Long id) {
        Boolean blocked = usuarioService.toggleBlocked(id);
        String msg = Boolean.TRUE.equals(blocked) ? "Usuario bloqueado" : "Usuario desbloqueado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, blocked));
    }

    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequestDto request) {
        Boolean result = usuarioService.resetPassword(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Contraseña restablecida exitosamente", false, result));
    }
}

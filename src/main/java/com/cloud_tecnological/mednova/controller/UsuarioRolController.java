package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.usuariorol.AsignarRolRequestDto;
import com.cloud_tecnological.mednova.dto.usuariorol.UsuarioRolResponseDto;
import com.cloud_tecnological.mednova.dto.usuariorol.UsuarioRolTableDto;
import com.cloud_tecnological.mednova.services.UsuarioRolService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-roles")
@PreAuthorize("isAuthenticated()")
public class UsuarioRolController {

    private final UsuarioRolService usuarioRolService;

    public UsuarioRolController(UsuarioRolService usuarioRolService) {
        this.usuarioRolService = usuarioRolService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioRolResponseDto>> assign(
            @Valid @RequestBody AsignarRolRequestDto request) {
        UsuarioRolResponseDto result = usuarioRolService.assign(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Rol asignado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioRolResponseDto>> findById(@PathVariable Long id) {
        UsuarioRolResponseDto result = usuarioRolService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<UsuarioRolTableDto>>> listAssignments(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<UsuarioRolTableDto> result = usuarioRolService.listAssignments(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UsuarioRolResponseDto>>> listByUser(@PathVariable Long userId) {
        List<UsuarioRolResponseDto> result = usuarioRolService.listByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> revoke(@PathVariable Long id) {
        Boolean result = usuarioRolService.revoke(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Asignación revocada exitosamente", false, result));
    }
}

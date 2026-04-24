package com.cloud_tecnological.mednova.controller;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloud_tecnological.mednova.dto.permiso.PermisoDto;
import com.cloud_tecnological.mednova.dto.rol.CreateRolRequestDto;
import com.cloud_tecnological.mednova.dto.rol.RolResponseDto;
import com.cloud_tecnological.mednova.dto.rol.RolTableDto;
import com.cloud_tecnological.mednova.dto.rol.UpdateRolRequestDto;
import com.cloud_tecnological.mednova.services.RolService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")

public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RolResponseDto>> create(
            @Valid @RequestBody CreateRolRequestDto request) {
        RolResponseDto result = rolService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Rol creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RolResponseDto>> findById(@PathVariable Long id) {
        RolResponseDto result = rolService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<RolTableDto>>> listRoles(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<RolTableDto> result = rolService.listRoles(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RolResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolRequestDto request) {
        RolResponseDto result = rolService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Rol actualizado", false, result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        Boolean result = rolService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Rol eliminado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = rolService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Rol activado" : "Rol inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<PermisoDto>>> listPermissions() {
        List<PermisoDto> result = rolService.listPermissions();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

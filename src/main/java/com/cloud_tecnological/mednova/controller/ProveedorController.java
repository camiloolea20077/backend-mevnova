package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.proveedor.CreateProveedorRequestDto;
import com.cloud_tecnological.mednova.dto.proveedor.ProveedorResponseDto;
import com.cloud_tecnological.mednova.dto.proveedor.ProveedorTableDto;
import com.cloud_tecnological.mednova.dto.proveedor.UpdateProveedorRequestDto;
import com.cloud_tecnological.mednova.services.ProveedorService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suppliers")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    // HU-FASE2-068: Crear proveedor
    @PostMapping
    public ResponseEntity<ApiResponse<ProveedorResponseDto>> create(
            @Valid @RequestBody CreateProveedorRequestDto request) {
        ProveedorResponseDto result = proveedorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Proveedor creado exitosamente", false, result));
    }

    // HU-FASE2-068: Consultar proveedor por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponseDto>> findById(@PathVariable Long id) {
        ProveedorResponseDto result = proveedorService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-068: Listar proveedores
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<ProveedorTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<ProveedorTableDto> result = proveedorService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-068: Actualizar proveedor
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProveedorRequestDto request) {
        ProveedorResponseDto result = proveedorService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Proveedor actualizado", false, result));
    }

    // HU-FASE2-068: Activar/Inactivar proveedor
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = proveedorService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Proveedor activado" : "Proveedor inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

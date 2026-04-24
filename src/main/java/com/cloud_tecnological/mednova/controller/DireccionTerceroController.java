package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.direcciontercero.CreateDireccionTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.DireccionTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.DireccionTerceroTableDto;
import com.cloud_tecnological.mednova.dto.direcciontercero.UpdateDireccionTerceroRequestDto;
import com.cloud_tecnological.mednova.services.DireccionTerceroService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/third-addresses")

public class DireccionTerceroController {

    private final DireccionTerceroService direccionTerceroService;

    public DireccionTerceroController(DireccionTerceroService direccionTerceroService) {
        this.direccionTerceroService = direccionTerceroService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DireccionTerceroResponseDto>> create(
            @Valid @RequestBody CreateDireccionTerceroRequestDto request) {
        DireccionTerceroResponseDto result = direccionTerceroService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Dirección creada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DireccionTerceroResponseDto>> findById(@PathVariable Long id) {
        DireccionTerceroResponseDto result = direccionTerceroService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/third/{thirdPartyId}")
    public ResponseEntity<ApiResponse<List<DireccionTerceroTableDto>>> listByThirdParty(
            @PathVariable Long thirdPartyId) {
        List<DireccionTerceroTableDto> result = direccionTerceroService.listByThirdParty(thirdPartyId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DireccionTerceroResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDireccionTerceroRequestDto request) {
        DireccionTerceroResponseDto result = direccionTerceroService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Dirección actualizada", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = direccionTerceroService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Dirección activada" : "Dirección inactivada";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

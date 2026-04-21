package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.relaciontercero.CreateRelacionTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.RelacionTerceroResponseDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.RelacionTerceroTableDto;
import com.cloud_tecnological.mednova.dto.relaciontercero.UpdateRelacionTerceroRequestDto;
import com.cloud_tecnological.mednova.services.RelacionTerceroService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/third-relations")
@PreAuthorize("isAuthenticated()")
public class RelacionTerceroController {

    private final RelacionTerceroService relacionTerceroService;

    public RelacionTerceroController(RelacionTerceroService relacionTerceroService) {
        this.relacionTerceroService = relacionTerceroService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RelacionTerceroResponseDto>> create(
            @Valid @RequestBody CreateRelacionTerceroRequestDto request) {
        RelacionTerceroResponseDto result = relacionTerceroService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Relación creada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RelacionTerceroResponseDto>> findById(@PathVariable Long id) {
        RelacionTerceroResponseDto result = relacionTerceroService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/third/{thirdPartyId}")
    public ResponseEntity<ApiResponse<List<RelacionTerceroTableDto>>> listByThirdParty(
            @PathVariable Long thirdPartyId) {
        List<RelacionTerceroTableDto> result = relacionTerceroService.listByThirdParty(thirdPartyId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RelacionTerceroResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRelacionTerceroRequestDto request) {
        RelacionTerceroResponseDto result = relacionTerceroService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Relación actualizada", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = relacionTerceroService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Relación activada" : "Relación inactivada";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.tercero.CreateTerceroRequestDto;
import com.cloud_tecnological.mednova.dto.tercero.TerceroResponseDto;
import com.cloud_tecnological.mednova.dto.tercero.TerceroTableDto;
import com.cloud_tecnological.mednova.dto.tercero.UpdateTerceroRequestDto;
import com.cloud_tecnological.mednova.services.TerceroService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thirds")

public class TerceroController {

    private final TerceroService terceroService;

    public TerceroController(TerceroService terceroService) {
        this.terceroService = terceroService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TerceroResponseDto>> create(
            @Valid @RequestBody CreateTerceroRequestDto request) {
        TerceroResponseDto result = terceroService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Tercero creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TerceroResponseDto>> findById(@PathVariable Long id) {
        TerceroResponseDto result = terceroService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/by-document")
    public ResponseEntity<ApiResponse<TerceroResponseDto>> findByDocument(
            @RequestParam Long documentTypeId,
            @RequestParam String documentNumber) {
        TerceroResponseDto result = terceroService.findByDocument(documentTypeId, documentNumber);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<TerceroTableDto>>> listTerceros(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<TerceroTableDto> result = terceroService.listTerceros(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TerceroResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTerceroRequestDto request) {
        TerceroResponseDto result = terceroService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Tercero actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = terceroService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Tercero activado" : "Tercero inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

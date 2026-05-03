package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.bodega.BodegaResponseDto;
import com.cloud_tecnological.mednova.dto.bodega.BodegaTableDto;
import com.cloud_tecnological.mednova.dto.bodega.CreateBodegaRequestDto;
import com.cloud_tecnological.mednova.dto.bodega.UpdateBodegaRequestDto;
import com.cloud_tecnological.mednova.services.BodegaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
public class BodegaController {

    private final BodegaService bodegaService;

    public BodegaController(BodegaService bodegaService) {
        this.bodegaService = bodegaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BodegaResponseDto>> create(
            @Valid @RequestBody CreateBodegaRequestDto request) {
        BodegaResponseDto result = bodegaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Bodega creada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BodegaResponseDto>> findById(@PathVariable Long id) {
        BodegaResponseDto result = bodegaService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<BodegaTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<BodegaTableDto> result = bodegaService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BodegaResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBodegaRequestDto request) {
        BodegaResponseDto result = bodegaService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Bodega actualizada", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = bodegaService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Bodega activada" : "Bodega inactivada";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

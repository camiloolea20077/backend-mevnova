package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.centrocosto.CentroCostoResponseDto;
import com.cloud_tecnological.mednova.dto.centrocosto.CentroCostoTableDto;
import com.cloud_tecnological.mednova.dto.centrocosto.CreateCentroCostoRequestDto;
import com.cloud_tecnological.mednova.dto.centrocosto.UpdateCentroCostoRequestDto;
import com.cloud_tecnological.mednova.services.CentroCostoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cost-centers")
public class CentroCostoController {

    private final CentroCostoService centroCostoService;

    public CentroCostoController(CentroCostoService centroCostoService) {
        this.centroCostoService = centroCostoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CentroCostoResponseDto>> create(
            @Valid @RequestBody CreateCentroCostoRequestDto request) {
        CentroCostoResponseDto result = centroCostoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Centro de costo creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CentroCostoResponseDto>> findById(@PathVariable Long id) {
        CentroCostoResponseDto result = centroCostoService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<CentroCostoTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<CentroCostoTableDto> result = centroCostoService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CentroCostoResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCentroCostoRequestDto request) {
        CentroCostoResponseDto result = centroCostoService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Centro de costo actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = centroCostoService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Centro de costo activado" : "Centro de costo inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

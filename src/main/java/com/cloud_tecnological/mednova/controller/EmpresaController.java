package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.empresa.*;
import com.cloud_tecnological.mednova.services.EmpresaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaResponseDto>> create(
            @Valid @RequestBody CreateEmpresaRequestDto request) {
        EmpresaResponseDto result = empresaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Empresa creada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponseDto>> findById(@PathVariable Long id) {
        EmpresaResponseDto result = empresaService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<EmpresaTableDto>>> findAll(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<EmpresaTableDto> result = empresaService.findAll(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmpresaRequestDto request) {
        EmpresaResponseDto result = empresaService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Empresa actualizada", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = empresaService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Empresa activada" : "Empresa desactivada";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

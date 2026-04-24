package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.contrato.*;
import com.cloud_tecnological.mednova.services.ContratoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contracts")
public class ContratoController {

    private final ContratoService contratoService;

    public ContratoController(ContratoService contratoService) {
        this.contratoService = contratoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContratoResponseDto>> create(
            @Valid @RequestBody CreateContratoRequestDto request) {
        ContratoResponseDto result = contratoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Contrato creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContratoResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, contratoService.findById(id)));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<ContratoTableDto>>> list(@RequestBody PageableDto<?> pageable) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, contratoService.list(pageable)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContratoResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContratoRequestDto request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Contrato actualizado", false, contratoService.update(id, request)));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = contratoService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Contrato activado" : "Contrato inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

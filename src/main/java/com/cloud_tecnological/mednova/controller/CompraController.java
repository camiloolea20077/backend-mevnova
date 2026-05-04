package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.compra.CancelCompraRequestDto;
import com.cloud_tecnological.mednova.dto.compra.CompraResponseDto;
import com.cloud_tecnological.mednova.dto.compra.CompraTableDto;
import com.cloud_tecnological.mednova.dto.compra.CreateCompraRequestDto;
import com.cloud_tecnological.mednova.services.CompraService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchases")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    // HU-FASE2-069: Crear compra en BORRADOR
    @PostMapping
    public ResponseEntity<ApiResponse<CompraResponseDto>> create(
            @Valid @RequestBody CreateCompraRequestDto request) {
        CompraResponseDto result = compraService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Compra creada en borrador", false, result));
    }

    // HU-FASE2-069: Consultar compra por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompraResponseDto>> findById(@PathVariable Long id) {
        CompraResponseDto result = compraService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-069: Listar compras
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<CompraTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<CompraTableDto> result = compraService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-069: Confirmar recepción (RECIBIDA → genera lote/stock/movimiento)
    @PatchMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<CompraResponseDto>> receive(@PathVariable Long id) {
        CompraResponseDto result = compraService.receive(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Compra recibida", false, result));
    }

    // HU-FASE2-069: Anular compra con justificación
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<CompraResponseDto>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelCompraRequestDto request) {
        CompraResponseDto result = compraService.cancel(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Compra anulada", false, result));
    }
}

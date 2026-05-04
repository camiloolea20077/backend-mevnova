package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.ajuste.AjusteInventarioResponseDto;
import com.cloud_tecnological.mednova.dto.ajuste.AjusteInventarioTableDto;
import com.cloud_tecnological.mednova.dto.ajuste.CancelAjusteRequestDto;
import com.cloud_tecnological.mednova.dto.ajuste.CreateAjusteInventarioRequestDto;
import com.cloud_tecnological.mednova.services.AjusteInventarioService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory-adjustments")
public class AjusteInventarioController {

    private final AjusteInventarioService ajusteService;

    public AjusteInventarioController(AjusteInventarioService ajusteService) {
        this.ajusteService = ajusteService;
    }

    // HU-FASE2-077: Crear ajuste de inventario en BORRADOR
    @PostMapping
    public ResponseEntity<ApiResponse<AjusteInventarioResponseDto>> create(
            @Valid @RequestBody CreateAjusteInventarioRequestDto request) {
        AjusteInventarioResponseDto result = ajusteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Ajuste creado en borrador", false, result));
    }

    // HU-FASE2-077: Consultar ajuste por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AjusteInventarioResponseDto>> findById(@PathVariable Long id) {
        AjusteInventarioResponseDto result = ajusteService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-077: Listar ajustes
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AjusteInventarioTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<AjusteInventarioTableDto> result = ajusteService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-077: Aprobar ajuste (segregación de funciones: aprobador != creador)
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<AjusteInventarioResponseDto>> approve(@PathVariable Long id) {
        AjusteInventarioResponseDto result = ajusteService.approve(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ajuste aprobado", false, result));
    }

    // HU-FASE2-077: Aplicar ajuste APROBADO (genera movimiento_inventario y actualiza stock_lote)
    @PatchMapping("/{id}/apply")
    public ResponseEntity<ApiResponse<AjusteInventarioResponseDto>> apply(@PathVariable Long id) {
        AjusteInventarioResponseDto result = ajusteService.apply(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ajuste aplicado", false, result));
    }

    // HU-FASE2-077: Anular ajuste con justificación (no permitido si está APLICADO)
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AjusteInventarioResponseDto>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelAjusteRequestDto request) {
        AjusteInventarioResponseDto result = ajusteService.cancel(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ajuste anulado", false, result));
    }
}

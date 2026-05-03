package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.detalleglosa.CreateDetalleGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.DetalleGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.DetalleGlosaTableDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.GlosaReconciliationDto;
import com.cloud_tecnological.mednova.dto.detalleglosa.UpdateDetalleGlosaRequestDto;
import com.cloud_tecnological.mednova.services.DetalleGlosaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/glosas")
public class DetalleGlosaController {

    private final DetalleGlosaService detalleGlosaService;

    public DetalleGlosaController(DetalleGlosaService detalleGlosaService) {
        this.detalleGlosaService = detalleGlosaService;
    }

    // HU-FASE2-063: Agregar ítem glosado
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<DetalleGlosaResponseDto>> create(
            @Valid @RequestBody CreateDetalleGlosaRequestDto request) {
        DetalleGlosaResponseDto result = detalleGlosaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Ítem de glosa registrado", false, result));
    }

    // HU-FASE2-063: Consultar detalle por ID
    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<DetalleGlosaResponseDto>> findById(@PathVariable Long id) {
        DetalleGlosaResponseDto result = detalleGlosaService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-063: Listar detalles de una glosa
    @GetMapping("/{glosaId}/items")
    public ResponseEntity<ApiResponse<List<DetalleGlosaTableDto>>> listByGlosa(
            @PathVariable Long glosaId) {
        List<DetalleGlosaTableDto> result = detalleGlosaService.listByGlosa(glosaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-063: Actualizar detalle (solo si glosa ABIERTA)
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<DetalleGlosaResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDetalleGlosaRequestDto request) {
        DetalleGlosaResponseDto result = detalleGlosaService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ítem de glosa actualizado", false, result));
    }

    // HU-FASE2-063: Eliminación lógica del detalle
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Boolean>> softDelete(@PathVariable Long id) {
        Boolean ok = detalleGlosaService.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Ítem de glosa eliminado", false, ok));
    }

    // HU-FASE2-063: Cuadre de la captura (suma de detalles vs valor total de la glosa)
    @GetMapping("/{glosaId}/items/reconciliation")
    public ResponseEntity<ApiResponse<GlosaReconciliationDto>> reconciliation(
            @PathVariable Long glosaId) {
        GlosaReconciliationDto result = detalleGlosaService.getReconciliation(glosaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

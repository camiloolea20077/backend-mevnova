package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.tarifario.*;
import com.cloud_tecnological.mednova.services.TarifarioService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rate-schedules")
public class TarifarioController {

    private final TarifarioService tarifarioService;

    public TarifarioController(TarifarioService tarifarioService) {
        this.tarifarioService = tarifarioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TarifarioResponseDto>> create(
            @Valid @RequestBody CreateTarifarioRequestDto request) {
        TarifarioResponseDto result = tarifarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Tarifario creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TarifarioResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, tarifarioService.findById(id)));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<TarifarioTableDto>>> list(@RequestBody PageableDto<?> pageable) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, tarifarioService.list(pageable)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TarifarioResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTarifarioRequestDto request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Tarifario actualizado", false, tarifarioService.update(id, request)));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = tarifarioService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Tarifario activado" : "Tarifario inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }

    // ---------- Detalle ----------

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<DetalleTarifarioResponseDto>> addDetalle(
            @PathVariable Long id,
            @Valid @RequestBody UpsertDetalleTarifarioRequestDto request) {
        DetalleTarifarioResponseDto result = tarifarioService.upsertDetalle(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Detalle agregado al tarifario", false, result));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<DetalleTarifarioResponseDto>>> listDetalles(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, tarifarioService.listDetalles(id)));
    }

    @GetMapping("/{id}/items/{detalleId}")
    public ResponseEntity<ApiResponse<DetalleTarifarioResponseDto>> findDetalleById(
            @PathVariable Long id, @PathVariable Long detalleId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, tarifarioService.findDetalleById(id, detalleId)));
    }

    @DeleteMapping("/{id}/items/{detalleId}")
    public ResponseEntity<ApiResponse<Boolean>> removeDetalle(
            @PathVariable Long id, @PathVariable Long detalleId) {
        tarifarioService.removeDetalle(id, detalleId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Detalle eliminado del tarifario", false, true));
    }
}

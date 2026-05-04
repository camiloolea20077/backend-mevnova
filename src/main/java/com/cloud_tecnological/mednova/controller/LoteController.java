package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.lote.LoteFilterParams;
import com.cloud_tecnological.mednova.dto.lote.LoteResponseDto;
import com.cloud_tecnological.mednova.dto.lote.LoteTableDto;
import com.cloud_tecnological.mednova.dto.lote.UpdateLoteRequestDto;
import com.cloud_tecnological.mednova.services.LoteService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batches")
public class LoteController {

    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    // HU-FASE2-070: Consultar lote por ID (incluye stock total)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoteResponseDto>> findById(@PathVariable Long id) {
        LoteResponseDto result = loteService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-070: Listar lotes con filtros (vencimiento próximo, vencidos, servicio)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<LoteTableDto>>> list(
            @RequestBody PageableDto<LoteFilterParams> pageable) {
        PageImpl<LoteTableDto> result = loteService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-070: Actualizar datos no críticos (registro INVIMA, observaciones)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LoteResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLoteRequestDto request) {
        LoteResponseDto result = loteService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Lote actualizado", false, result));
    }

    // HU-FASE2-070: Activar/Inactivar lote (bloqueado si tiene stock)
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = loteService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Lote activado" : "Lote inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

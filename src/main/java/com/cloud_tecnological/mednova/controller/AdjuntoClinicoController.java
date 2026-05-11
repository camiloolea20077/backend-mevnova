package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoFilterParams;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoResponseDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.AdjuntoClinicoTableDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.CreateAdjuntoClinicoRequestDto;
import com.cloud_tecnological.mednova.dto.adjuntoclinico.UpdateAdjuntoClinicoRequestDto;
import com.cloud_tecnological.mednova.services.AdjuntoClinicoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clinical-attachments")
public class AdjuntoClinicoController {

    private final AdjuntoClinicoService service;

    public AdjuntoClinicoController(AdjuntoClinicoService service) {
        this.service = service;
    }

    // HU-FASE2-092 CA1: Cargar adjunto a la HC del paciente
    @PostMapping
    public ResponseEntity<ApiResponse<AdjuntoClinicoResponseDto>> create(
            @Valid @RequestBody CreateAdjuntoClinicoRequestDto request) {
        AdjuntoClinicoResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Adjunto cargado", false, result));
    }

    // HU-FASE2-092: Consultar por ID (CA3: confidencial requiere permiso)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdjuntoClinicoResponseDto>> findById(@PathVariable Long id) {
        AdjuntoClinicoResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-092 CA2+CA3: Listar adjuntos (oculta confidenciales si no tiene permiso)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AdjuntoClinicoTableDto>>> list(
            @RequestBody PageableDto<AdjuntoClinicoFilterParams> pageable) {
        PageImpl<AdjuntoClinicoTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-092: Actualizar metadatos del adjunto
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdjuntoClinicoResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdjuntoClinicoRequestDto request) {
        AdjuntoClinicoResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Adjunto actualizado", false, result));
    }

    // HU-FASE2-092 CA3: Marcar como confidencial / quitar marca
    @PatchMapping("/{id}/confidential")
    public ResponseEntity<ApiResponse<AdjuntoClinicoResponseDto>> setConfidential(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean confidential = body != null && Boolean.TRUE.equals(body.get("confidential"));
        AdjuntoClinicoResponseDto result = service.setConfidential(id, confidential);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
                confidential ? "Adjunto marcado como confidencial" : "Marca de confidencialidad retirada",
                false, result));
    }

    // HU-FASE2-092: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Adjunto eliminado", false, null));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.diagnostico.CatalogoDiagnosticoSearchDto;
import com.cloud_tecnological.mednova.dto.diagnostico.CreateDiagnosticoRequestDto;
import com.cloud_tecnological.mednova.dto.diagnostico.DiagnosticoResponseDto;
import com.cloud_tecnological.mednova.services.DiagnosticoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnoses")
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    public DiagnosticoController(DiagnosticoService diagnosticoService) {
        this.diagnosticoService = diagnosticoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DiagnosticoResponseDto>> create(
            @Valid @RequestBody CreateDiagnosticoRequestDto request) {
        DiagnosticoResponseDto result = diagnosticoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Diagnóstico registrado", false, result));
    }

    @GetMapping("/by-attention/{atencionId}")
    public ResponseEntity<ApiResponse<List<DiagnosticoResponseDto>>> findByAtencionId(
            @PathVariable Long atencionId) {
        List<DiagnosticoResponseDto> result = diagnosticoService.findByAtencionId(atencionId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        Boolean result = diagnosticoService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Diagnóstico eliminado", false, result));
    }

    @GetMapping("/catalog/search")
    public ResponseEntity<ApiResponse<List<CatalogoDiagnosticoSearchDto>>> searchCatalogo(
            @RequestParam("q") String q) {
        List<CatalogoDiagnosticoSearchDto> result = diagnosticoService.searchCatalogo(q);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

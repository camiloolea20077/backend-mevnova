package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.impactoglosa.ImpactoGlosaCarteraDto;
import com.cloud_tecnological.mednova.services.ImpactoGlosaCarteraService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/glosas")
public class ImpactoGlosaCarteraController {

    private final ImpactoGlosaCarteraService impactoGlosaCarteraService;

    public ImpactoGlosaCarteraController(ImpactoGlosaCarteraService impactoGlosaCarteraService) {
        this.impactoGlosaCarteraService = impactoGlosaCarteraService;
    }

    // HU-FASE2-066: Consultar el movimiento de cartera generado por una glosa cerrada
    @GetMapping("/{glosaId}/cartera-impact")
    public ResponseEntity<ApiResponse<ImpactoGlosaCarteraDto>> getImpacto(
            @PathVariable Long glosaId) {
        ImpactoGlosaCarteraDto result = impactoGlosaCarteraService.consultarImpacto(glosaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

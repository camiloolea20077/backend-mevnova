package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.cobertura.CoberturaVerificacionResponseDto;
import com.cloud_tecnological.mednova.services.CoberturaVerificacionService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients/{patientId}/coverage-verification")
public class CoberturaVerificacionController {

    private final CoberturaVerificacionService coberturaVerificacionService;

    public CoberturaVerificacionController(CoberturaVerificacionService coberturaVerificacionService) {
        this.coberturaVerificacionService = coberturaVerificacionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CoberturaVerificacionResponseDto>> verify(
            @PathVariable Long patientId,
            @RequestParam(required = false) Long serviceId) {
        CoberturaVerificacionResponseDto result = coberturaVerificacionService.verify(patientId, serviceId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Verificación de cobertura completada", false, result));
    }
}

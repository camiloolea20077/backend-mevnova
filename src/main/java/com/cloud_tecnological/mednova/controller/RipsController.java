package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.rips.RipsLineaDto;
import com.cloud_tecnological.mednova.dto.rips.RipsResponseDto;
import com.cloud_tecnological.mednova.services.RipsService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing/rips")
public class RipsController {

    private final RipsService ripsService;

    public RipsController(RipsService ripsService) {
        this.ripsService = ripsService;
    }

    // HU-057: Generar RIPS desde una factura APROBADA
    @PostMapping("/generate/{facturaId}")
    public ResponseEntity<ApiResponse<RipsResponseDto>> generarDesdeFactura(
            @PathVariable Long facturaId,
            @RequestBody(required = false) Map<String, String> body) {
        String obs = body != null ? body.get("observations") : null;
        RipsResponseDto result = ripsService.generarDesdeFactura(facturaId, obs);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "RIPS generado exitosamente", false, result));
    }

    // HU-057: Consultar RIPS por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RipsResponseDto>> findById(@PathVariable Long id) {
        RipsResponseDto result = ripsService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-057: Consultar RIPS de una factura
    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<ApiResponse<RipsResponseDto>> findByFactura(@PathVariable Long facturaId) {
        RipsResponseDto result = ripsService.findByFactura(facturaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-057: Consultar líneas de un RIPS
    @GetMapping("/{id}/lines")
    public ResponseEntity<ApiResponse<List<RipsLineaDto>>> findLineas(@PathVariable Long id) {
        List<RipsLineaDto> result = ripsService.findLineas(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

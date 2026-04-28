package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.liquidacion.LiquidacionResponseDto;
import com.cloud_tecnological.mednova.services.LiquidacionService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/liquidaciones")
public class LiquidacionController {

    private final LiquidacionService liquidacionService;

    public LiquidacionController(LiquidacionService liquidacionService) {
        this.liquidacionService = liquidacionService;
    }

    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<ApiResponse<List<LiquidacionResponseDto>>> findByFactura(
            @PathVariable Long facturaId) {
        List<LiquidacionResponseDto> result = liquidacionService.findByFactura(facturaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<ApiResponse<List<LiquidacionResponseDto>>> findByPaciente(
            @PathVariable Long pacienteId) {
        List<LiquidacionResponseDto> result = liquidacionService.findByPaciente(pacienteId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

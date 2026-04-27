package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.ordenclinica.CreateOrdenClinicaRequestDto;
import com.cloud_tecnological.mednova.dto.ordenclinica.OrdenClinicaResponseDto;
import com.cloud_tecnological.mednova.services.OrdenClinicaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical-orders")
public class OrdenClinicaController {

    private final OrdenClinicaService ordenClinicaService;

    public OrdenClinicaController(OrdenClinicaService ordenClinicaService) {
        this.ordenClinicaService = ordenClinicaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrdenClinicaResponseDto>> create(
            @Valid @RequestBody CreateOrdenClinicaRequestDto request) {
        OrdenClinicaResponseDto result = ordenClinicaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Orden clínica registrada", false, result));
    }

    @GetMapping("/by-attention/{atencionId}")
    public ResponseEntity<ApiResponse<List<OrdenClinicaResponseDto>>> findByAtencionId(
            @PathVariable Long atencionId) {
        List<OrdenClinicaResponseDto> result = ordenClinicaService.findByAtencionId(atencionId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        Boolean result = ordenClinicaService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Orden clínica eliminada", false, result));
    }
}

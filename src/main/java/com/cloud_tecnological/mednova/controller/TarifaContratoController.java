package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.tarifacontrato.CreateTarifaContratoRequestDto;
import com.cloud_tecnological.mednova.dto.tarifacontrato.TarifaContratoResponseDto;
import com.cloud_tecnological.mednova.dto.tarifacontrato.UpdateTarifaContratoRequestDto;
import com.cloud_tecnological.mednova.services.TarifaContratoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts/{contractId}/rates")
public class TarifaContratoController {

    private final TarifaContratoService tarifaContratoService;

    public TarifaContratoController(TarifaContratoService tarifaContratoService) {
        this.tarifaContratoService = tarifaContratoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TarifaContratoResponseDto>> create(
            @PathVariable Long contractId,
            @Valid @RequestBody CreateTarifaContratoRequestDto request) {
        TarifaContratoResponseDto result = tarifaContratoService.create(contractId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Tarifa de contrato creada exitosamente", false, result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TarifaContratoResponseDto>>> list(@PathVariable Long contractId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                tarifaContratoService.listByContrato(contractId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TarifaContratoResponseDto>> findById(
            @PathVariable Long contractId, @PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                tarifaContratoService.findById(contractId, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TarifaContratoResponseDto>> update(
            @PathVariable Long contractId, @PathVariable Long id,
            @Valid @RequestBody UpdateTarifaContratoRequestDto request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Tarifa actualizada", false,
                tarifaContratoService.update(contractId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> remove(
            @PathVariable Long contractId, @PathVariable Long id) {
        tarifaContratoService.remove(contractId, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Tarifa eliminada del contrato", false, true));
    }
}

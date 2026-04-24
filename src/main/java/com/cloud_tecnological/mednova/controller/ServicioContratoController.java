package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.serviciocontrato.CreateServicioContratoRequestDto;
import com.cloud_tecnological.mednova.dto.serviciocontrato.ServicioContratoResponseDto;
import com.cloud_tecnological.mednova.dto.serviciocontrato.UpdateServicioContratoRequestDto;
import com.cloud_tecnological.mednova.services.ServicioContratoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts/{contractId}/coverage-services")
public class ServicioContratoController {

    private final ServicioContratoService servicioContratoService;

    public ServicioContratoController(ServicioContratoService servicioContratoService) {
        this.servicioContratoService = servicioContratoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServicioContratoResponseDto>> create(
            @PathVariable Long contractId,
            @Valid @RequestBody CreateServicioContratoRequestDto request) {
        ServicioContratoResponseDto result = servicioContratoService.create(contractId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Servicio agregado al contrato exitosamente", false, result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicioContratoResponseDto>>> list(@PathVariable Long contractId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                servicioContratoService.listByContrato(contractId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioContratoResponseDto>> findById(
            @PathVariable Long contractId, @PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                servicioContratoService.findById(contractId, id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioContratoResponseDto>> update(
            @PathVariable Long contractId, @PathVariable Long id,
            @Valid @RequestBody UpdateServicioContratoRequestDto request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Servicio del contrato actualizado", false,
                servicioContratoService.update(contractId, id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> remove(
            @PathVariable Long contractId, @PathVariable Long id) {
        servicioContratoService.remove(contractId, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Servicio eliminado del contrato", false, true));
    }
}

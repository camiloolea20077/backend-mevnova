package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.sisbenpaciente.CreateSisbenPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.sisbenpaciente.SisbenPacienteResponseDto;
import com.cloud_tecnological.mednova.services.SisbenPacienteService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/sisben")
public class SisbenPacienteController {

    private final SisbenPacienteService sisbenPacienteService;

    public SisbenPacienteController(SisbenPacienteService sisbenPacienteService) {
        this.sisbenPacienteService = sisbenPacienteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SisbenPacienteResponseDto>> create(
            @PathVariable Long patientId,
            @Valid @RequestBody CreateSisbenPacienteRequestDto request) {
        SisbenPacienteResponseDto result = sisbenPacienteService.create(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Registro SISBEN creado exitosamente", false, result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SisbenPacienteResponseDto>>> list(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                sisbenPacienteService.listByPaciente(patientId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SisbenPacienteResponseDto>> findById(
            @PathVariable Long patientId, @PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                sisbenPacienteService.findById(patientId, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> remove(
            @PathVariable Long patientId, @PathVariable Long id) {
        sisbenPacienteService.remove(patientId, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Registro SISBEN eliminado", false, true));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.contratopaciente.ContratoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.contratopaciente.CreateContratoPacienteRequestDto;
import com.cloud_tecnological.mednova.services.ContratoPacienteService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/contracts")
public class ContratoPacienteController {

    private final ContratoPacienteService contratoPacienteService;

    public ContratoPacienteController(ContratoPacienteService contratoPacienteService) {
        this.contratoPacienteService = contratoPacienteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ContratoPacienteResponseDto>> create(
            @PathVariable Long patientId,
            @Valid @RequestBody CreateContratoPacienteRequestDto request) {
        ContratoPacienteResponseDto result = contratoPacienteService.create(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Contrato asociado al paciente exitosamente", false, result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ContratoPacienteResponseDto>>> list(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                contratoPacienteService.listByPaciente(patientId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContratoPacienteResponseDto>> findById(
            @PathVariable Long patientId, @PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                contratoPacienteService.findById(patientId, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> remove(
            @PathVariable Long patientId, @PathVariable Long id) {
        contratoPacienteService.remove(patientId, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Contrato desasociado del paciente", false, true));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.CreateSeguridadSocialPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.seguridadsocialpaciente.SeguridadSocialPacienteResponseDto;
import com.cloud_tecnological.mednova.services.SeguridadSocialPacienteService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/social-security")
public class SeguridadSocialPacienteController {

    private final SeguridadSocialPacienteService seguridadSocialPacienteService;

    public SeguridadSocialPacienteController(SeguridadSocialPacienteService seguridadSocialPacienteService) {
        this.seguridadSocialPacienteService = seguridadSocialPacienteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SeguridadSocialPacienteResponseDto>> create(
            @PathVariable Long patientId,
            @Valid @RequestBody CreateSeguridadSocialPacienteRequestDto request) {
        SeguridadSocialPacienteResponseDto result = seguridadSocialPacienteService.create(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Afiliación de seguridad social creada exitosamente", false, result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SeguridadSocialPacienteResponseDto>>> list(@PathVariable Long patientId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                seguridadSocialPacienteService.listByPaciente(patientId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeguridadSocialPacienteResponseDto>> findById(
            @PathVariable Long patientId, @PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false,
                seguridadSocialPacienteService.findById(patientId, id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> remove(
            @PathVariable Long patientId, @PathVariable Long id) {
        seguridadSocialPacienteService.remove(patientId, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Afiliación de seguridad social eliminada", false, true));
    }
}

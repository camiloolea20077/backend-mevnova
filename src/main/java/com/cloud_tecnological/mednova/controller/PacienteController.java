package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.paciente.CreatePacienteRequestDto;
import com.cloud_tecnological.mednova.dto.paciente.PacienteResponseDto;
import com.cloud_tecnological.mednova.dto.paciente.PacienteTableDto;
import com.cloud_tecnological.mednova.dto.paciente.UpdatePacienteRequestDto;
import com.cloud_tecnological.mednova.services.PacienteService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")

public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PacienteResponseDto>> create(
            @Valid @RequestBody CreatePacienteRequestDto request) {
        PacienteResponseDto result = pacienteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Paciente creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PacienteResponseDto>> findById(@PathVariable Long id) {
        PacienteResponseDto result = pacienteService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/third/{thirdPartyId}")
    public ResponseEntity<ApiResponse<PacienteResponseDto>> findByThirdParty(@PathVariable Long thirdPartyId) {
        PacienteResponseDto result = pacienteService.findByThirdParty(thirdPartyId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<PacienteTableDto>>> listPacientes(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<PacienteTableDto> result = pacienteService.listPacientes(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PacienteResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePacienteRequestDto request) {
        PacienteResponseDto result = pacienteService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Paciente actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = pacienteService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Paciente activado" : "Paciente inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

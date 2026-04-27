package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.atencion.*;
import com.cloud_tecnological.mednova.services.AtencionService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attentions")
public class AtencionController {

    private final AtencionService atencionService;

    public AtencionController(AtencionService atencionService) {
        this.atencionService = atencionService;
    }

    @GetMapping("/by-admission/{admisionId}")
    public ResponseEntity<ApiResponse<AtencionResponseDto>> findByAdmisionId(
            @PathVariable Long admisionId) {
        AtencionResponseDto result = atencionService.findByAdmisionId(admisionId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PatchMapping("/{id}/triage")
    public ResponseEntity<ApiResponse<AtencionResponseDto>> registrarTriage(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarTriageRequestDto request) {
        AtencionResponseDto result = atencionService.registrarTriage(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Triage registrado exitosamente", false, result));
    }

    @PatchMapping("/{id}/triage/reclassify")
    public ResponseEntity<ApiResponse<AtencionResponseDto>> reclasificarTriage(
            @PathVariable Long id,
            @Valid @RequestBody ReclasificarTriageRequestDto request) {
        AtencionResponseDto result = atencionService.reclasificarTriage(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Triage reclasificado exitosamente", false, result));
    }

    @GetMapping("/emergency-queue")
    public ResponseEntity<ApiResponse<List<ColaUrgenciasDto>>> getColaUrgencias() {
        List<ColaUrgenciasDto> result = atencionService.getColaUrgencias();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/{id}/console")
    public ResponseEntity<ApiResponse<ConsolaAtencionDto>> getConsola(@PathVariable Long id) {
        ConsolaAtencionDto result = atencionService.getConsola(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<AtencionResponseDto>> startAttention(@PathVariable Long id) {
        AtencionResponseDto result = atencionService.startAttention(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Atención iniciada", false, result));
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<AtencionResponseDto>> actualizarNotas(
            @PathVariable Long id,
            @RequestBody ActualizarNotasRequestDto request) {
        AtencionResponseDto result = atencionService.actualizarNotas(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Notas actualizadas", false, result));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<AtencionResponseDto>> cerrarAtencion(
            @PathVariable Long id,
            @Valid @RequestBody CerrarAtencionRequestDto request) {
        AtencionResponseDto result = atencionService.cerrarAtencion(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Atención cerrada exitosamente", false, result));
    }

    @PatchMapping("/{id}/hospitalization-request")
    public ResponseEntity<ApiResponse<AtencionResponseDto>> solicitarHospitalizacion(
            @PathVariable Long id,
            @Valid @RequestBody SolicitarHospitalizacionRequestDto request) {
        AtencionResponseDto result = atencionService.solicitarHospitalizacion(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Solicitud de hospitalización registrada", false, result));
    }
}

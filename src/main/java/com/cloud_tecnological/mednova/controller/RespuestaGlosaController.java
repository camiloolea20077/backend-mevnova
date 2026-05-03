package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.respuestaglosa.CreateRespuestaGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.respuestaglosa.RespuestaGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.respuestaglosa.UpdateRespuestaGlosaRequestDto;
import com.cloud_tecnological.mednova.services.RespuestaGlosaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/glosas")
public class RespuestaGlosaController {

    private final RespuestaGlosaService respuestaGlosaService;

    public RespuestaGlosaController(RespuestaGlosaService respuestaGlosaService) {
        this.respuestaGlosaService = respuestaGlosaService;
    }

    // HU-FASE2-064: Crear respuesta a un ítem de glosa
    @PostMapping("/responses")
    public ResponseEntity<ApiResponse<RespuestaGlosaResponseDto>> create(
            @Valid @RequestBody CreateRespuestaGlosaRequestDto request) {
        RespuestaGlosaResponseDto result = respuestaGlosaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Respuesta de glosa registrada", false, result));
    }

    // HU-FASE2-064: Consultar respuesta por ID
    @GetMapping("/responses/{id}")
    public ResponseEntity<ApiResponse<RespuestaGlosaResponseDto>> findById(@PathVariable Long id) {
        RespuestaGlosaResponseDto result = respuestaGlosaService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-064: Consultar respuesta por detalle de glosa
    @GetMapping("/items/{glossDetailId}/response")
    public ResponseEntity<ApiResponse<RespuestaGlosaResponseDto>> findByDetalle(
            @PathVariable Long glossDetailId) {
        RespuestaGlosaResponseDto result = respuestaGlosaService.findByGlossDetail(glossDetailId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-064: Listar respuestas de una glosa
    @GetMapping("/{glosaId}/responses")
    public ResponseEntity<ApiResponse<List<RespuestaGlosaResponseDto>>> listByGloss(
            @PathVariable Long glosaId) {
        List<RespuestaGlosaResponseDto> result = respuestaGlosaService.listByGloss(glosaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-064: Actualizar respuesta
    @PutMapping("/responses/{id}")
    public ResponseEntity<ApiResponse<RespuestaGlosaResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRespuestaGlosaRequestDto request) {
        RespuestaGlosaResponseDto result = respuestaGlosaService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Respuesta actualizada", false, result));
    }

    // HU-FASE2-064: Eliminación lógica de la respuesta
    @DeleteMapping("/responses/{id}")
    public ResponseEntity<ApiResponse<Boolean>> softDelete(@PathVariable Long id) {
        Boolean ok = respuestaGlosaService.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Respuesta eliminada", false, ok));
    }
}

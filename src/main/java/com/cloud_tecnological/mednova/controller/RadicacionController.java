package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.radicacion.CrearRadicacionRequestDto;
import com.cloud_tecnological.mednova.dto.radicacion.RadicacionResponseDto;
import com.cloud_tecnological.mednova.dto.radicacion.RadicacionTableDto;
import com.cloud_tecnological.mednova.dto.radicacion.RegistrarRespuestaRequestDto;
import com.cloud_tecnological.mednova.services.RadicacionService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/radicaciones")
public class RadicacionController {

    private final RadicacionService radicacionService;

    public RadicacionController(RadicacionService radicacionService) {
        this.radicacionService = radicacionService;
    }

    // HU-058: Crear radicación
    @PostMapping
    public ResponseEntity<ApiResponse<RadicacionResponseDto>> crear(
            @Valid @RequestBody CrearRadicacionRequestDto dto) {
        RadicacionResponseDto result = radicacionService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Radicación creada exitosamente", false, result));
    }

    // HU-058: Consultar radicación por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RadicacionResponseDto>> findById(@PathVariable Long id) {
        RadicacionResponseDto result = radicacionService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-058: Listar radicaciones paginado
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<RadicacionTableDto>>> listActive(
            @RequestBody PageableDto<?> request) {
        PageImpl<RadicacionTableDto> result = radicacionService.listActive(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-058: Registrar respuesta del pagador
    @PatchMapping("/{id}/respuesta")
    public ResponseEntity<ApiResponse<RadicacionResponseDto>> registrarRespuesta(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarRespuestaRequestDto dto) {
        RadicacionResponseDto result = radicacionService.registrarRespuesta(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Respuesta registrada exitosamente", false, result));
    }

    // HU-058: Anular radicación
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> anular(@PathVariable Long id) {
        radicacionService.anular(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Radicación anulada", false, true));
    }
}

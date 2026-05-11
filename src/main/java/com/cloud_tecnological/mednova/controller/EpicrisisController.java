package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.epicrisis.CreateEpicrisisRequestDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisFilterParams;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisPreloadDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisResponseDto;
import com.cloud_tecnological.mednova.dto.epicrisis.EpicrisisTableDto;
import com.cloud_tecnological.mednova.dto.epicrisis.SignEpicrisisRequestDto;
import com.cloud_tecnological.mednova.dto.epicrisis.UpdateEpicrisisRequestDto;
import com.cloud_tecnological.mednova.services.EpicrisisService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discharge-summaries")
public class EpicrisisController {

    private final EpicrisisService service;

    public EpicrisisController(EpicrisisService service) {
        this.service = service;
    }

    // HU-FASE2-091 CA1: Crear epicrisis al egreso
    @PostMapping
    public ResponseEntity<ApiResponse<EpicrisisResponseDto>> create(
            @Valid @RequestBody CreateEpicrisisRequestDto request) {
        EpicrisisResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Epicrisis creada", false, result));
    }

    // HU-FASE2-091 CA2: Precarga estructurada desde la admisión
    @GetMapping("/preload/{admissionId}")
    public ResponseEntity<ApiResponse<EpicrisisPreloadDto>> preload(@PathVariable Long admissionId) {
        EpicrisisPreloadDto result = service.preload(admissionId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-091: Consultar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EpicrisisResponseDto>> findById(@PathVariable Long id) {
        EpicrisisResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-091: Listado paginado
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<EpicrisisTableDto>>> list(
            @RequestBody PageableDto<EpicrisisFilterParams> pageable) {
        PageImpl<EpicrisisTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-091: Actualizar (CA3: bloqueado si está firmada)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EpicrisisResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEpicrisisRequestDto request) {
        EpicrisisResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Epicrisis actualizada", false, result));
    }

    // HU-FASE2-091 CA3+CA4: Firmar epicrisis (bloquea edición, opcionalmente guarda pdfUrl)
    @PatchMapping("/{id}/sign")
    public ResponseEntity<ApiResponse<EpicrisisResponseDto>> sign(
            @PathVariable Long id,
            @RequestBody(required = false) SignEpicrisisRequestDto request) {
        EpicrisisResponseDto result = service.sign(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Epicrisis firmada", false, result));
    }

    // HU-FASE2-091: Eliminación lógica (no aplica a firmada)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Epicrisis eliminada", false, null));
    }
}

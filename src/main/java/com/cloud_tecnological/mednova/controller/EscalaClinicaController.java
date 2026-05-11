package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.escalaclinica.CreateEscalaClinicaRequestDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaFilterParams;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaResponseDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.EscalaClinicaTableDto;
import com.cloud_tecnological.mednova.dto.escalaclinica.UpdateEscalaClinicaRequestDto;
import com.cloud_tecnological.mednova.services.EscalaClinicaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinical-scales")
public class EscalaClinicaController {

    private final EscalaClinicaService service;

    public EscalaClinicaController(EscalaClinicaService service) {
        this.service = service;
    }

    // HU-FASE2-089 CA1+CA3: Aplicar escala (cálculo automático de riesgo, asociada a atención)
    @PostMapping
    public ResponseEntity<ApiResponse<EscalaClinicaResponseDto>> create(
            @Valid @RequestBody CreateEscalaClinicaRequestDto request) {
        EscalaClinicaResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Escala clínica registrada", false, result));
    }

    // HU-FASE2-089: Consultar por ID con detalle JSON
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EscalaClinicaResponseDto>> findById(@PathVariable Long id) {
        EscalaClinicaResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-089 CA2: Listado paginado (filtrar por tipo de escala + ORDER ASC = evolución)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<EscalaClinicaTableDto>>> list(
            @RequestBody PageableDto<EscalaClinicaFilterParams> pageable) {
        PageImpl<EscalaClinicaTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-089: Corrección de captura (no es aplicación nueva)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EscalaClinicaResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEscalaClinicaRequestDto request) {
        EscalaClinicaResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Escala clínica actualizada", false, result));
    }

    // HU-FASE2-089: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Escala clínica eliminada", false, null));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarFilterParams;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedenteFamiliarTableDto;
import com.cloud_tecnological.mednova.dto.antecedente.CreateAntecedenteFamiliarRequestDto;
import com.cloud_tecnological.mednova.dto.antecedente.UpdateAntecedenteFamiliarRequestDto;
import com.cloud_tecnological.mednova.services.AntecedenteFamiliarService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/family-antecedents")
public class AntecedenteFamiliarController {

    private final AntecedenteFamiliarService service;

    public AntecedenteFamiliarController(AntecedenteFamiliarService service) {
        this.service = service;
    }

    // HU-FASE2-080: Crear antecedente familiar
    @PostMapping
    public ResponseEntity<ApiResponse<AntecedenteFamiliarResponseDto>> create(
            @Valid @RequestBody CreateAntecedenteFamiliarRequestDto request) {
        AntecedenteFamiliarResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Antecedente familiar registrado", false, result));
    }

    // HU-FASE2-080: Consultar antecedente familiar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AntecedenteFamiliarResponseDto>> findById(@PathVariable Long id) {
        AntecedenteFamiliarResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-080: Listar antecedentes familiares (filtrar por paciente, parentesco, fallecidos)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AntecedenteFamiliarTableDto>>> list(
            @RequestBody PageableDto<AntecedenteFamiliarFilterParams> pageable) {
        PageImpl<AntecedenteFamiliarTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-080: Actualizar antecedente familiar
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AntecedenteFamiliarResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAntecedenteFamiliarRequestDto request) {
        AntecedenteFamiliarResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Antecedente familiar actualizado", false, result));
    }

    // HU-FASE2-080: Activar / inactivar antecedente familiar
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AntecedenteFamiliarResponseDto>> activate(@PathVariable Long id) {
        AntecedenteFamiliarResponseDto result = service.setActive(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Antecedente familiar activado", false, result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<AntecedenteFamiliarResponseDto>> deactivate(@PathVariable Long id) {
        AntecedenteFamiliarResponseDto result = service.setActive(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Antecedente familiar inactivado", false, result));
    }

    // HU-FASE2-080: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Antecedente familiar eliminado", false, null));
    }
}

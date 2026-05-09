package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalFilterParams;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.AntecedentePersonalTableDto;
import com.cloud_tecnological.mednova.dto.antecedente.CreateAntecedentePersonalRequestDto;
import com.cloud_tecnological.mednova.dto.antecedente.TipoAntecedenteResponseDto;
import com.cloud_tecnological.mednova.dto.antecedente.UpdateAntecedentePersonalRequestDto;
import com.cloud_tecnological.mednova.services.AntecedentePersonalService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal-antecedents")
public class AntecedentePersonalController {

    private final AntecedentePersonalService service;

    public AntecedentePersonalController(AntecedentePersonalService service) {
        this.service = service;
    }

    // HU-FASE2-079: Crear antecedente personal
    @PostMapping
    public ResponseEntity<ApiResponse<AntecedentePersonalResponseDto>> create(
            @Valid @RequestBody CreateAntecedentePersonalRequestDto request) {
        AntecedentePersonalResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Antecedente registrado", false, result));
    }

    // HU-FASE2-079: Consultar antecedente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AntecedentePersonalResponseDto>> findById(@PathVariable Long id) {
        AntecedentePersonalResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-079: Listar antecedentes (filtrar por paciente, tipo, alergias)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AntecedentePersonalTableDto>>> list(
            @RequestBody PageableDto<AntecedentePersonalFilterParams> pageable) {
        PageImpl<AntecedentePersonalTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-079: Actualizar antecedente
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AntecedentePersonalResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAntecedentePersonalRequestDto request) {
        AntecedentePersonalResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Antecedente actualizado", false, result));
    }

    // HU-FASE2-079: Activar / inactivar antecedente (no se elimina, queda histórico)
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<AntecedentePersonalResponseDto>> activate(@PathVariable Long id) {
        AntecedentePersonalResponseDto result = service.setActive(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Antecedente activado", false, result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<AntecedentePersonalResponseDto>> deactivate(@PathVariable Long id) {
        AntecedentePersonalResponseDto result = service.setActive(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Antecedente inactivado", false, result));
    }

    // HU-FASE2-079: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Antecedente eliminado", false, null));
    }

    // HU-FASE2-079: Catálogo de tipos de antecedente (lectura para selectores)
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<TipoAntecedenteResponseDto>>> listTypes() {
        List<TipoAntecedenteResponseDto> result = service.listAntecedentTypes();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

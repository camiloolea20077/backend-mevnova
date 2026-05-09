package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.revision.CreateRevisionSistemasRequestDto;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasFilterParams;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasResponseDto;
import com.cloud_tecnological.mednova.dto.revision.RevisionSistemasTableDto;
import com.cloud_tecnological.mednova.dto.revision.UpdateRevisionSistemasRequestDto;
import com.cloud_tecnological.mednova.services.RevisionSistemasService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system-reviews")
public class RevisionSistemasController {

    private final RevisionSistemasService service;

    public RevisionSistemasController(RevisionSistemasService service) {
        this.service = service;
    }

    // HU-FASE2-082: Crear revisión por sistema
    @PostMapping
    public ResponseEntity<ApiResponse<RevisionSistemasResponseDto>> create(
            @Valid @RequestBody CreateRevisionSistemasRequestDto request) {
        RevisionSistemasResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Revisión por sistemas registrada", false, result));
    }

    // HU-FASE2-082: Consultar revisión por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RevisionSistemasResponseDto>> findById(@PathVariable Long id) {
        RevisionSistemasResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-082: Listar revisiones (filtrar por atención, paciente, sistema)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<RevisionSistemasTableDto>>> list(
            @RequestBody PageableDto<RevisionSistemasFilterParams> pageable) {
        PageImpl<RevisionSistemasTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-082: Actualizar revisión
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RevisionSistemasResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRevisionSistemasRequestDto request) {
        RevisionSistemasResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Revisión por sistemas actualizada", false, result));
    }

    // HU-FASE2-082: Activar / inactivar
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<RevisionSistemasResponseDto>> activate(@PathVariable Long id) {
        RevisionSistemasResponseDto result = service.setActive(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Revisión activada", false, result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<RevisionSistemasResponseDto>> deactivate(@PathVariable Long id) {
        RevisionSistemasResponseDto result = service.setActive(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Revisión inactivada", false, result));
    }

    // HU-FASE2-082: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Revisión eliminada", false, null));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.plancuidados.ChangePlanStatusRequestDto;
import com.cloud_tecnological.mednova.dto.plancuidados.CreatePlanCuidadosRequestDto;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosFilterParams;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosResponseDto;
import com.cloud_tecnological.mednova.dto.plancuidados.PlanCuidadosTableDto;
import com.cloud_tecnological.mednova.dto.plancuidados.UpdatePlanCuidadosRequestDto;
import com.cloud_tecnological.mednova.services.PlanCuidadosService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nursing-care-plans")
public class PlanCuidadosController {

    private final PlanCuidadosService service;

    public PlanCuidadosController(PlanCuidadosService service) {
        this.service = service;
    }

    // HU-FASE2-085: Crear plan de cuidados
    @PostMapping
    public ResponseEntity<ApiResponse<PlanCuidadosResponseDto>> create(
            @Valid @RequestBody CreatePlanCuidadosRequestDto request) {
        PlanCuidadosResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Plan de cuidados creado", false, result));
    }

    // HU-FASE2-085: Consultar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanCuidadosResponseDto>> findById(@PathVariable Long id) {
        PlanCuidadosResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-085: Listar planes (filtrar por atención, paciente, profesional, estado)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<PlanCuidadosTableDto>>> list(
            @RequestBody PageableDto<PlanCuidadosFilterParams> pageable) {
        PageImpl<PlanCuidadosTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-085: Actualizar plan
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PlanCuidadosResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePlanCuidadosRequestDto request) {
        PlanCuidadosResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Plan de cuidados actualizado", false, result));
    }

    // HU-FASE2-085: Cambiar estado del plan (CUMPLIDO, MODIFICADO, SUSPENDIDO, ACTIVO)
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PlanCuidadosResponseDto>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangePlanStatusRequestDto request) {
        PlanCuidadosResponseDto result = service.changeStatus(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Estado del plan actualizado", false, result));
    }

    // HU-FASE2-085: Activar / inactivar (estado lógico)
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<PlanCuidadosResponseDto>> activate(@PathVariable Long id) {
        PlanCuidadosResponseDto result = service.setActive(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Plan activado", false, result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<PlanCuidadosResponseDto>> deactivate(@PathVariable Long id) {
        PlanCuidadosResponseDto result = service.setActive(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Plan inactivado", false, result));
    }

    // HU-FASE2-085: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Plan eliminado", false, null));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.medicacion.CreateMedicacionHabitualRequestDto;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualFilterParams;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualResponseDto;
import com.cloud_tecnological.mednova.dto.medicacion.MedicacionHabitualTableDto;
import com.cloud_tecnological.mednova.dto.medicacion.UpdateMedicacionHabitualRequestDto;
import com.cloud_tecnological.mednova.services.MedicacionHabitualService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/regular-medications")
public class MedicacionHabitualController {

    private final MedicacionHabitualService service;

    public MedicacionHabitualController(MedicacionHabitualService service) {
        this.service = service;
    }

    // HU-FASE2-084: Crear medicación habitual
    @PostMapping
    public ResponseEntity<ApiResponse<MedicacionHabitualResponseDto>> create(
            @Valid @RequestBody CreateMedicacionHabitualRequestDto request) {
        MedicacionHabitualResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Medicación habitual registrada", false, result));
    }

    // HU-FASE2-084: Consultar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicacionHabitualResponseDto>> findById(@PathVariable Long id) {
        MedicacionHabitualResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-084: Listado paginado por paciente y filtros
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<MedicacionHabitualTableDto>>> list(
            @RequestBody PageableDto<MedicacionHabitualFilterParams> pageable) {
        PageImpl<MedicacionHabitualTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-084: Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicacionHabitualResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicacionHabitualRequestDto request) {
        MedicacionHabitualResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Medicación actualizada", false, result));
    }

    // HU-FASE2-084: Marcar el medicamento como vigente / suspendido (CA2)
    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<MedicacionHabitualResponseDto>> markAsTaking(@PathVariable Long id) {
        MedicacionHabitualResponseDto result = service.setCurrentlyTaking(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Medicación marcada como vigente", false, result));
    }

    @PatchMapping("/{id}/stop")
    public ResponseEntity<ApiResponse<MedicacionHabitualResponseDto>> markAsStopped(@PathVariable Long id) {
        MedicacionHabitualResponseDto result = service.setCurrentlyTaking(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Medicación marcada como suspendida", false, result));
    }

    // HU-FASE2-084: Activar / inactivar (estado lógico)
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<MedicacionHabitualResponseDto>> activate(@PathVariable Long id) {
        MedicacionHabitualResponseDto result = service.setActive(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Medicación activada", false, result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<MedicacionHabitualResponseDto>> deactivate(@PathVariable Long id) {
        MedicacionHabitualResponseDto result = service.setActive(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Medicación inactivada", false, result));
    }

    // HU-FASE2-084: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Medicación eliminada", false, null));
    }
}

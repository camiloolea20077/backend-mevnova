package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.habito.CreateHabitoPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteFilterParams;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.habito.HabitoPacienteTableDto;
import com.cloud_tecnological.mednova.dto.habito.UpdateHabitoPacienteRequestDto;
import com.cloud_tecnological.mednova.services.HabitoPacienteService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient-habits")
public class HabitoPacienteController {

    private final HabitoPacienteService service;

    public HabitoPacienteController(HabitoPacienteService service) {
        this.service = service;
    }

    // HU-FASE2-081: Crear hábito
    @PostMapping
    public ResponseEntity<ApiResponse<HabitoPacienteResponseDto>> create(
            @Valid @RequestBody CreateHabitoPacienteRequestDto request) {
        HabitoPacienteResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Hábito registrado", false, result));
    }

    // HU-FASE2-081: Consultar hábito por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HabitoPacienteResponseDto>> findById(@PathVariable Long id) {
        HabitoPacienteResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-081: Listar hábitos (filtrar por paciente, tipo, estado)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<HabitoPacienteTableDto>>> list(
            @RequestBody PageableDto<HabitoPacienteFilterParams> pageable) {
        PageImpl<HabitoPacienteTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-081: Actualizar hábito
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HabitoPacienteResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHabitoPacienteRequestDto request) {
        HabitoPacienteResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Hábito actualizado", false, result));
    }

    // HU-FASE2-081: Activar / inactivar hábito
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<HabitoPacienteResponseDto>> activate(@PathVariable Long id) {
        HabitoPacienteResponseDto result = service.setActive(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Hábito activado", false, result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<HabitoPacienteResponseDto>> deactivate(@PathVariable Long id) {
        HabitoPacienteResponseDto result = service.setActive(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Hábito inactivado", false, result));
    }

    // HU-FASE2-081: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Hábito eliminado", false, null));
    }
}

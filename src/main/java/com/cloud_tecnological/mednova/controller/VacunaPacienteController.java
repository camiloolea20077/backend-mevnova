package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.vacuna.CreateVacunaPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.vacuna.UpdateVacunaPacienteRequestDto;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteFilterParams;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteResponseDto;
import com.cloud_tecnological.mednova.dto.vacuna.VacunaPacienteTableDto;
import com.cloud_tecnological.mednova.services.VacunaPacienteService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient-vaccines")
public class VacunaPacienteController {

    private final VacunaPacienteService service;

    public VacunaPacienteController(VacunaPacienteService service) {
        this.service = service;
    }

    // HU-FASE2-083: Crear aplicación de vacuna
    @PostMapping
    public ResponseEntity<ApiResponse<VacunaPacienteResponseDto>> create(
            @Valid @RequestBody CreateVacunaPacienteRequestDto request) {
        VacunaPacienteResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Vacuna registrada", false, result));
    }

    // HU-FASE2-083: Consultar vacuna por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VacunaPacienteResponseDto>> findById(@PathVariable Long id) {
        VacunaPacienteResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-083: Cronología de vacunas (filtrar por paciente, código, próxima dosis)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<VacunaPacienteTableDto>>> list(
            @RequestBody PageableDto<VacunaPacienteFilterParams> pageable) {
        PageImpl<VacunaPacienteTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-083: Actualizar vacuna
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VacunaPacienteResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVacunaPacienteRequestDto request) {
        VacunaPacienteResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Vacuna actualizada", false, result));
    }

    // HU-FASE2-083: Activar / inactivar
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<VacunaPacienteResponseDto>> activate(@PathVariable Long id) {
        VacunaPacienteResponseDto result = service.setActive(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Vacuna activada", false, result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<VacunaPacienteResponseDto>> deactivate(@PathVariable Long id) {
        VacunaPacienteResponseDto result = service.setActive(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Vacuna inactivada", false, result));
    }

    // HU-FASE2-083: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Vacuna eliminada", false, null));
    }
}

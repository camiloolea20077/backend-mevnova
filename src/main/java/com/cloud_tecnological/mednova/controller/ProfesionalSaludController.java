package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.profesionalsalud.CreateProfesionalSaludRequestDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.ProfesionalSaludResponseDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.ProfesionalSaludTableDto;
import com.cloud_tecnological.mednova.dto.profesionalsalud.UpdateProfesionalSaludRequestDto;
import com.cloud_tecnological.mednova.services.ProfesionalSaludService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health-professionals")
@PreAuthorize("isAuthenticated()")
public class ProfesionalSaludController {

    private final ProfesionalSaludService profesionalSaludService;

    public ProfesionalSaludController(ProfesionalSaludService profesionalSaludService) {
        this.profesionalSaludService = profesionalSaludService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProfesionalSaludResponseDto>> create(
            @Valid @RequestBody CreateProfesionalSaludRequestDto request) {
        ProfesionalSaludResponseDto result = profesionalSaludService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Profesional de salud creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfesionalSaludResponseDto>> findById(@PathVariable Long id) {
        ProfesionalSaludResponseDto result = profesionalSaludService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/third/{thirdPartyId}")
    public ResponseEntity<ApiResponse<ProfesionalSaludResponseDto>> findByThirdParty(@PathVariable Long thirdPartyId) {
        ProfesionalSaludResponseDto result = profesionalSaludService.findByThirdParty(thirdPartyId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<ProfesionalSaludTableDto>>> listProfesionales(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<ProfesionalSaludTableDto> result = profesionalSaludService.listProfesionales(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfesionalSaludResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfesionalSaludRequestDto request) {
        ProfesionalSaludResponseDto result = profesionalSaludService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Profesional de salud actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = profesionalSaludService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Profesional activado" : "Profesional inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

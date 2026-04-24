package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.serviciosalud.CreateServicioSaludRequestDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.ServicioSaludResponseDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.ServicioSaludTableDto;
import com.cloud_tecnological.mednova.dto.serviciosalud.UpdateServicioSaludRequestDto;
import com.cloud_tecnological.mednova.services.ServicioSaludService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health-services")
public class ServicioSaludController {

    private final ServicioSaludService servicioSaludService;

    public ServicioSaludController(ServicioSaludService servicioSaludService) {
        this.servicioSaludService = servicioSaludService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServicioSaludResponseDto>> create(
            @Valid @RequestBody CreateServicioSaludRequestDto request) {
        ServicioSaludResponseDto result = servicioSaludService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Servicio de salud creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioSaludResponseDto>> findById(@PathVariable Long id) {
        ServicioSaludResponseDto result = servicioSaludService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<ServicioSaludTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<ServicioSaludTableDto> result = servicioSaludService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioSaludResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServicioSaludRequestDto request) {
        ServicioSaludResponseDto result = servicioSaludService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Servicio de salud actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = servicioSaludService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Servicio activado" : "Servicio inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

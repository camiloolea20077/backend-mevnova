package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.serviciohabilitado.CreateServicioHabilitadoRequestDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.ServicioHabilitadoResponseDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.ServicioHabilitadoTableDto;
import com.cloud_tecnological.mednova.dto.serviciohabilitado.UpdateServicioHabilitadoRequestDto;
import com.cloud_tecnological.mednova.services.ServicioHabilitadoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enabled-services")
@PreAuthorize("isAuthenticated()")
public class ServicioHabilitadoController {

    private final ServicioHabilitadoService servicioHabilitadoService;

    public ServicioHabilitadoController(ServicioHabilitadoService servicioHabilitadoService) {
        this.servicioHabilitadoService = servicioHabilitadoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServicioHabilitadoResponseDto>> create(
            @Valid @RequestBody CreateServicioHabilitadoRequestDto request) {
        ServicioHabilitadoResponseDto result = servicioHabilitadoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Servicio habilitado creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioHabilitadoResponseDto>> findById(@PathVariable Long id) {
        ServicioHabilitadoResponseDto result = servicioHabilitadoService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<ServicioHabilitadoTableDto>>> listServices(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<ServicioHabilitadoTableDto> result = servicioHabilitadoService.listServices(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioHabilitadoResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServicioHabilitadoRequestDto request) {
        ServicioHabilitadoResponseDto result = servicioHabilitadoService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Servicio habilitado actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = servicioHabilitadoService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Servicio activado" : "Servicio inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

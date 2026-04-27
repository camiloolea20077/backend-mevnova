package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.cita.*;
import com.cloud_tecnological.mednova.services.CitaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> create(
            @Valid @RequestBody CreateAppointmentRequestDto request) {
        AppointmentResponseDto result = citaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Cita asignada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, citaService.findById(id)));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AppointmentTableDto>>> listActive(
            @RequestBody PageableDto<?> request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, citaService.listActive(request)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Boolean>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequestDto request) {
        Boolean result = citaService.cancel(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cita cancelada exitosamente", false, result));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> reschedule(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequestDto request) {
        AppointmentResponseDto result = citaService.reschedule(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Cita reprogramada exitosamente", false, result));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.disponibilidad.DisponibilidadResponseDto;
import com.cloud_tecnological.mednova.dto.disponibilidad.GenerateAvailabilityRequestDto;
import com.cloud_tecnological.mednova.dto.disponibilidad.SearchDisponibilidadRequestDto;
import com.cloud_tecnological.mednova.services.DisponibilidadService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Integer>> generateAvailability(
            @Valid @RequestBody GenerateAvailabilityRequestDto request) {
        Integer slotsCreated = disponibilidadService.generateAvailability(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(),
            "Disponibilidad generada: " + slotsCreated + " slots procesados", false, slotsCreated));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<DisponibilidadResponseDto>>> search(
            @RequestBody SearchDisponibilidadRequestDto request) {
        List<DisponibilidadResponseDto> result = disponibilidadService.search(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

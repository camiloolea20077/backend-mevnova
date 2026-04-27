package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.prescripcion.CreatePrescripcionRequestDto;
import com.cloud_tecnological.mednova.dto.prescripcion.PrescripcionResponseDto;
import com.cloud_tecnological.mednova.services.PrescripcionService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescripcionController {

    private final PrescripcionService prescripcionService;

    public PrescripcionController(PrescripcionService prescripcionService) {
        this.prescripcionService = prescripcionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PrescripcionResponseDto>> create(
            @Valid @RequestBody CreatePrescripcionRequestDto request) {
        PrescripcionResponseDto result = prescripcionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Prescripción registrada", false, result));
    }

    @GetMapping("/by-attention/{atencionId}")
    public ResponseEntity<ApiResponse<List<PrescripcionResponseDto>>> findByAtencionId(
            @PathVariable Long atencionId) {
        List<PrescripcionResponseDto> result = prescripcionService.findByAtencionId(atencionId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        Boolean result = prescripcionService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Prescripción eliminada", false, result));
    }
}

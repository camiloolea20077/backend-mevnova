package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.concertacionglosa.ConcertacionGlosaResponseDto;
import com.cloud_tecnological.mednova.dto.concertacionglosa.CreateConcertacionGlosaRequestDto;
import com.cloud_tecnological.mednova.services.ConcertacionGlosaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/glosas")
public class ConcertacionGlosaController {

    private final ConcertacionGlosaService concertacionGlosaService;

    public ConcertacionGlosaController(ConcertacionGlosaService concertacionGlosaService) {
        this.concertacionGlosaService = concertacionGlosaService;
    }

    // HU-FASE2-065: Crear concertación (cierra la glosa)
    @PostMapping("/concertations")
    public ResponseEntity<ApiResponse<ConcertacionGlosaResponseDto>> create(
            @Valid @RequestBody CreateConcertacionGlosaRequestDto request) {
        ConcertacionGlosaResponseDto result = concertacionGlosaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Glosa conciliada y cerrada exitosamente", false, result));
    }

    // HU-FASE2-065: Consultar concertación por ID
    @GetMapping("/concertations/{id}")
    public ResponseEntity<ApiResponse<ConcertacionGlosaResponseDto>> findById(@PathVariable Long id) {
        ConcertacionGlosaResponseDto result = concertacionGlosaService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-065: Consultar concertación por glosa
    @GetMapping("/{glosaId}/concertation")
    public ResponseEntity<ApiResponse<ConcertacionGlosaResponseDto>> findByGloss(
            @PathVariable Long glosaId) {
        ConcertacionGlosaResponseDto result = concertacionGlosaService.findByGloss(glosaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

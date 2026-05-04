package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.traslado.CreateTrasladoRequestDto;
import com.cloud_tecnological.mednova.dto.traslado.TrasladoResponseDto;
import com.cloud_tecnological.mednova.services.TrasladoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory-transfers")
public class TrasladoController {

    private final TrasladoService trasladoService;

    public TrasladoController(TrasladoService trasladoService) {
        this.trasladoService = trasladoService;
    }

    // HU-FASE2-076: Traslado entre bodegas de la misma empresa (puede cruzar sedes)
    @PostMapping
    public ResponseEntity<ApiResponse<TrasladoResponseDto>> transfer(
            @Valid @RequestBody CreateTrasladoRequestDto request) {
        TrasladoResponseDto result = trasladoService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Traslado registrado", false, result));
    }
}

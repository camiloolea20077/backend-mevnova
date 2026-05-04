package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.devolucion.CreateDevolucionRequestDto;
import com.cloud_tecnological.mednova.dto.devolucion.DevolucionItemResponseDto;
import com.cloud_tecnological.mednova.dto.dispensacion.CancelDispensacionRequestDto;
import com.cloud_tecnological.mednova.dto.dispensacion.CreateDispensacionRequestDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensacionResponseDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensacionTableDto;
import com.cloud_tecnological.mednova.dto.dispensacion.DispensationSuggestionItemDto;
import com.cloud_tecnological.mednova.services.DevolucionService;
import com.cloud_tecnological.mednova.services.DispensacionService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispensations")
public class DispensacionController {

    private final DispensacionService dispensacionService;
    private final DevolucionService   devolucionService;

    public DispensacionController(DispensacionService dispensacionService,
                                  DevolucionService devolucionService) {
        this.dispensacionService = dispensacionService;
        this.devolucionService   = devolucionService;
    }

    // HU-FASE2-074: Crear dispensación contra prescripción activa de la sede
    @PostMapping
    public ResponseEntity<ApiResponse<DispensacionResponseDto>> create(
            @Valid @RequestBody CreateDispensacionRequestDto request) {
        DispensacionResponseDto result = dispensacionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Dispensación creada exitosamente", false, result));
    }

    // HU-FASE2-074: Consultar dispensación por ID (incluye items)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DispensacionResponseDto>> findById(@PathVariable Long id) {
        DispensacionResponseDto result = dispensacionService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-074: Listar dispensaciones de la sede
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<DispensacionTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<DispensacionTableDto> result = dispensacionService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-074: Sugerencias FEFO por prescripción y bodega
    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<DispensationSuggestionItemDto>>> getSuggestions(
            @RequestParam("prescriptionId") Long prescriptionId,
            @RequestParam("warehouseId") Long warehouseId) {
        List<DispensationSuggestionItemDto> result = dispensacionService.getSuggestions(prescriptionId, warehouseId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-074: Anular dispensación con justificación (revierte stock)
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<DispensacionResponseDto>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelDispensacionRequestDto request) {
        DispensacionResponseDto result = dispensacionService.cancel(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Dispensación anulada", false, result));
    }

    // HU-FASE2-075: Registrar devolución a farmacia contra una dispensación previa
    @PostMapping("/{id}/returns")
    public ResponseEntity<ApiResponse<List<DevolucionItemResponseDto>>> registerReturn(
            @PathVariable Long id,
            @Valid @RequestBody CreateDevolucionRequestDto request) {
        List<DevolucionItemResponseDto> result = devolucionService.registerReturn(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Devolución registrada", false, result));
    }
}

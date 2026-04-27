package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.calendario.*;
import com.cloud_tecnological.mednova.services.CalendarioService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendars")
public class CalendarioController {

    private final CalendarioService calendarioService;

    public CalendarioController(CalendarioService calendarioService) {
        this.calendarioService = calendarioService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CalendarioResponseDto>> create(
            @Valid @RequestBody CreateCalendarioRequestDto request) {
        CalendarioResponseDto result = calendarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Calendario creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CalendarioResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, calendarioService.findById(id)));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<CalendarioTableDto>>> listActivos(
            @RequestBody PageableDto<?> request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, calendarioService.listActivos(request)));
    }

    @PostMapping("/{id}/detalles")
    public ResponseEntity<ApiResponse<CalendarioResponseDto>> addDetalle(
            @PathVariable Long id,
            @Valid @RequestBody AddDetalleCalendarioRequestDto request) {
        CalendarioResponseDto result = calendarioService.addDetalle(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Detalle agregado exitosamente", false, result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        Boolean result = calendarioService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Calendario eliminado", false, result));
    }
}

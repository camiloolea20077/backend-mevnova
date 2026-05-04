package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.solicitudmedicamento.CancelSolicitudMedicamentoRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.CreateSolicitudMedicamentoRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DispatchSolicitudRequestDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.DispatchSuggestionItemDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.SolicitudMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.solicitudmedicamento.SolicitudMedicamentoTableDto;
import com.cloud_tecnological.mednova.services.SolicitudMedicamentoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medication-requests")
public class SolicitudMedicamentoController {

    private final SolicitudMedicamentoService solicitudService;

    public SolicitudMedicamentoController(SolicitudMedicamentoService solicitudService) {
        this.solicitudService = solicitudService;
    }

    // HU-FASE2-072: Crear solicitud de medicamento (estado PENDIENTE)
    @PostMapping
    public ResponseEntity<ApiResponse<SolicitudMedicamentoResponseDto>> create(
            @Valid @RequestBody CreateSolicitudMedicamentoRequestDto request) {
        SolicitudMedicamentoResponseDto result = solicitudService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Solicitud creada exitosamente", false, result));
    }

    // HU-FASE2-072: Consultar solicitud por ID (incluye items)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SolicitudMedicamentoResponseDto>> findById(@PathVariable Long id) {
        SolicitudMedicamentoResponseDto result = solicitudService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-072: Listar solicitudes (orden VITAL → URGENTE → NORMAL por defecto)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<SolicitudMedicamentoTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<SolicitudMedicamentoTableDto> result = solicitudService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-072: Anular solicitud pendiente con justificación
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SolicitudMedicamentoResponseDto>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancelSolicitudMedicamentoRequestDto request) {
        SolicitudMedicamentoResponseDto result = solicitudService.cancel(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Solicitud anulada", false, result));
    }

    // HU-FASE2-073: Sugerencia FEFO por cada ítem pendiente
    @GetMapping("/{id}/dispatch-suggestion")
    public ResponseEntity<ApiResponse<List<DispatchSuggestionItemDto>>> getDispatchSuggestion(@PathVariable Long id) {
        List<DispatchSuggestionItemDto> result = solicitudService.getDispatchSuggestions(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-073: Despachar solicitud (descuenta origen, incrementa destino, genera movimientos)
    @PatchMapping("/{id}/dispatch")
    public ResponseEntity<ApiResponse<SolicitudMedicamentoResponseDto>> dispatch(
            @PathVariable Long id,
            @Valid @RequestBody DispatchSolicitudRequestDto request) {
        SolicitudMedicamentoResponseDto result = solicitudService.dispatch(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Despacho procesado", false, result));
    }
}

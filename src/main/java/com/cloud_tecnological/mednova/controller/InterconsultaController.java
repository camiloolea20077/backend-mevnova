package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.interconsulta.CreateInterconsultaRequestDto;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaFilterParams;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaResponseDto;
import com.cloud_tecnological.mednova.dto.interconsulta.InterconsultaTableDto;
import com.cloud_tecnological.mednova.dto.interconsulta.RespondInterconsultaRequestDto;
import com.cloud_tecnological.mednova.dto.interconsulta.UpdateInterconsultaRequestDto;
import com.cloud_tecnological.mednova.services.InterconsultaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/interconsultations")
public class InterconsultaController {

    private final InterconsultaService service;

    public InterconsultaController(InterconsultaService service) {
        this.service = service;
    }

    // HU-FASE2-090 CA1: Solicitar interconsulta
    @PostMapping
    public ResponseEntity<ApiResponse<InterconsultaResponseDto>> create(
            @Valid @RequestBody CreateInterconsultaRequestDto request) {
        InterconsultaResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Interconsulta creada", false, result));
    }

    // HU-FASE2-090: Consultar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterconsultaResponseDto>> findById(@PathVariable Long id) {
        InterconsultaResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-090 CA2: Bandeja paginada (orden default por prioridad VITAL primero)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<InterconsultaTableDto>>> list(
            @RequestBody PageableDto<InterconsultaFilterParams> pageable) {
        PageImpl<InterconsultaTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-090: Editar solicitud (solo PENDIENTE)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InterconsultaResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInterconsultaRequestDto request) {
        InterconsultaResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Interconsulta actualizada", false, result));
    }

    // HU-FASE2-090: Tomar interconsulta (PENDIENTE → EN_PROCESO)
    @PatchMapping("/{id}/take")
    public ResponseEntity<ApiResponse<InterconsultaResponseDto>> take(
            @PathVariable Long id,
            @Valid @RequestBody TakeInterconsultaRequest body) {
        InterconsultaResponseDto result = service.markInProgress(id, body.getRespondingProfessionalId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Interconsulta en proceso", false, result));
    }

    // HU-FASE2-090 CA3: Responder interconsulta (estado → RESPONDIDA, enlaza atención de respuesta)
    @PatchMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<InterconsultaResponseDto>> respond(
            @PathVariable Long id,
            @Valid @RequestBody RespondInterconsultaRequestDto request) {
        InterconsultaResponseDto result = service.respond(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Interconsulta respondida", false, result));
    }

    // HU-FASE2-090: Anular interconsulta
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InterconsultaResponseDto>> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        InterconsultaResponseDto result = service.cancel(id, reason);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Interconsulta anulada", false, result));
    }

    // HU-FASE2-090: Eliminación lógica
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Interconsulta eliminada", false, null));
    }

    @Getter
    @Setter
    public static class TakeInterconsultaRequest {
        @NotNull(message = "respondingProfessionalId es obligatorio")
        private Long respondingProfessionalId;
    }
}

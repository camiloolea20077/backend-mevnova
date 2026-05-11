package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministerDoseRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoFilterParams;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoResponseDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdministracionMedicamentoTableDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.AdverseReactionRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.OmitDoseRequestDto;
import com.cloud_tecnological.mednova.dto.administracionmedicamento.ScheduleAdministracionRequestDto;
import com.cloud_tecnological.mednova.services.AdministracionMedicamentoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medication-administrations")
public class AdministracionMedicamentoController {

    private final AdministracionMedicamentoService service;

    public AdministracionMedicamentoController(AdministracionMedicamentoService service) {
        this.service = service;
    }

    // HU-FASE2-087 CA1: Programar dosis (sistema genera dosis a partir de la prescripción)
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<List<AdministracionMedicamentoResponseDto>>> schedule(
            @Valid @RequestBody ScheduleAdministracionRequestDto request) {
        List<AdministracionMedicamentoResponseDto> result = service.schedule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                        "Dosis programadas (" + result.size() + ")", false, result));
    }

    // HU-FASE2-087 CA2: Administrar dosis con trazabilidad de lote
    @PatchMapping("/{id}/administer")
    public ResponseEntity<ApiResponse<AdministracionMedicamentoResponseDto>> administer(
            @PathVariable Long id,
            @Valid @RequestBody AdministerDoseRequestDto request) {
        AdministracionMedicamentoResponseDto result = service.administer(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Dosis administrada", false, result));
    }

    // HU-FASE2-087 CA3: Omitir dosis (OMITIDA o RECHAZADA) con motivo
    @PatchMapping("/{id}/omit")
    public ResponseEntity<ApiResponse<AdministracionMedicamentoResponseDto>> omit(
            @PathVariable Long id,
            @Valid @RequestBody OmitDoseRequestDto request) {
        AdministracionMedicamentoResponseDto result = service.omit(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Dosis " + result.getStatus().toLowerCase(), false, result));
    }

    // HU-FASE2-087: Suspender dosis programada
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<AdministracionMedicamentoResponseDto>> suspend(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        AdministracionMedicamentoResponseDto result = service.suspend(id, reason);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Dosis suspendida", false, result));
    }

    // HU-FASE2-087 CA4: Registrar reacción adversa
    @PatchMapping("/{id}/adverse-reaction")
    public ResponseEntity<ApiResponse<AdministracionMedicamentoResponseDto>> registerAdverseReaction(
            @PathVariable Long id,
            @Valid @RequestBody AdverseReactionRequestDto request) {
        AdministracionMedicamentoResponseDto result = service.registerAdverseReaction(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Reacción adversa registrada", false, result));
    }

    // HU-FASE2-087: Consultar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdministracionMedicamentoResponseDto>> findById(@PathVariable Long id) {
        AdministracionMedicamentoResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-087: Panel MAR / listado paginado (filtra por atención, paciente, estado, fechas)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AdministracionMedicamentoTableDto>>> list(
            @RequestBody PageableDto<AdministracionMedicamentoFilterParams> pageable) {
        PageImpl<AdministracionMedicamentoTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-087: Eliminación lógica (solo dosis no administradas)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Dosis eliminada", false, null));
    }
}

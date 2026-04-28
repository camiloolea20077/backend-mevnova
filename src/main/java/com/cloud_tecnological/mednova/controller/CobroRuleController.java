package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.cobrorule.*;
import com.cloud_tecnological.mednova.services.CobroRuleService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/cobro-rules")
public class CobroRuleController {

    private final CobroRuleService cobroRuleService;

    public CobroRuleController(CobroRuleService cobroRuleService) {
        this.cobroRuleService = cobroRuleService;
    }

    // ---- Reglas de cobro ----

    @PostMapping
    public ResponseEntity<ApiResponse<CobroRuleResponseDto>> create(
            @Valid @RequestBody CreateCobroRuleRequestDto request) {
        CobroRuleResponseDto result = cobroRuleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Regla de cobro creada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CobroRuleResponseDto>> findById(@PathVariable Long id) {
        CobroRuleResponseDto result = cobroRuleService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> update(
            @PathVariable Long id,
            @RequestBody UpdateCobroRuleRequestDto request) {
        Boolean result = cobroRuleService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Regla actualizada exitosamente", false, result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        Boolean result = cobroRuleService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Regla eliminada exitosamente", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<CobroRuleTableDto>>> listActive(
            @RequestBody PageableDto<?> request) {
        PageImpl<CobroRuleTableDto> result = cobroRuleService.listActive(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // ---- Exenciones ----

    @PostMapping("/exemptions")
    public ResponseEntity<ApiResponse<ServiceExemptionResponseDto>> createExemption(
            @Valid @RequestBody CreateServiceExemptionRequestDto request) {
        ServiceExemptionResponseDto result = cobroRuleService.createExemption(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Exención creada exitosamente", false, result));
    }

    @GetMapping("/exemptions/{id}")
    public ResponseEntity<ApiResponse<ServiceExemptionResponseDto>> findExemptionById(@PathVariable Long id) {
        ServiceExemptionResponseDto result = cobroRuleService.findExemptionById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @DeleteMapping("/exemptions/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteExemption(@PathVariable Long id) {
        Boolean result = cobroRuleService.deleteExemption(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Exención eliminada exitosamente", false, result));
    }

    @PostMapping("/exemptions/list")
    public ResponseEntity<ApiResponse<PageImpl<ServiceExemptionTableDto>>> listExemptions(
            @RequestBody PageableDto<?> request) {
        PageImpl<ServiceExemptionTableDto> result = cobroRuleService.listExemptions(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

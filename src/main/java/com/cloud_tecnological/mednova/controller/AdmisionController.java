package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.admision.AdmisionResponseDto;
import com.cloud_tecnological.mednova.dto.admision.AdmisionTableDto;
import com.cloud_tecnological.mednova.dto.admision.ChangeAdmisionStatusRequestDto;
import com.cloud_tecnological.mednova.dto.admision.CreateAdmisionRequestDto;
import com.cloud_tecnological.mednova.dto.admision.EgresoAdmisionRequestDto;
import com.cloud_tecnological.mednova.services.AdmisionService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admissions")
public class AdmisionController {

    private final AdmisionService admisionService;

    public AdmisionController(AdmisionService admisionService) {
        this.admisionService = admisionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdmisionResponseDto>> create(
            @Valid @RequestBody CreateAdmisionRequestDto request) {
        AdmisionResponseDto result = admisionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Admisión registrada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdmisionResponseDto>> findById(@PathVariable Long id) {
        AdmisionResponseDto result = admisionService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AdmisionTableDto>>> listActive(
            @RequestBody PageableDto<?> request) {
        PageImpl<AdmisionTableDto> result = admisionService.listActive(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Boolean>> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeAdmisionStatusRequestDto request) {
        Boolean result = admisionService.changeStatus(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Estado actualizado", false, result));
    }

    @PatchMapping("/{id}/egreso")
    public ResponseEntity<ApiResponse<Boolean>> egreso(
            @PathVariable Long id,
            @Valid @RequestBody EgresoAdmisionRequestDto request) {
        Boolean result = admisionService.egreso(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Egreso registrado exitosamente", false, result));
    }
}

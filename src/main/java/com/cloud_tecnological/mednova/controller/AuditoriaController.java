package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.auditoria.AuditoriaFilterDto;
import com.cloud_tecnological.mednova.dto.auditoria.AuditoriaResponseDto;
import com.cloud_tecnological.mednova.services.AuditoriaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("isAuthenticated()")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditoriaResponseDto>> findById(@PathVariable Long id) {
        AuditoriaResponseDto result = auditoriaService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AuditoriaResponseDto>>> listAuditoria(
            @RequestBody PageableDto<AuditoriaFilterDto> pageable) {
        PageImpl<AuditoriaResponseDto> result = auditoriaService.listAuditoria(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

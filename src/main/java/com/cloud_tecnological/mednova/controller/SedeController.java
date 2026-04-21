package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.sede.CreateSedeRequestDto;
import com.cloud_tecnological.mednova.dto.sede.SedeResponseDto;
import com.cloud_tecnological.mednova.dto.sede.SedeTableDto;
import com.cloud_tecnological.mednova.dto.sede.UpdateSedeRequestDto;
import com.cloud_tecnological.mednova.services.SedeService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/branches")
@PreAuthorize("isAuthenticated()")
public class SedeController {

    private final SedeService sedeService;

    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SedeResponseDto>> create(
            @Valid @RequestBody CreateSedeRequestDto request) {
        SedeResponseDto result = sedeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Sede creada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SedeResponseDto>> findById(@PathVariable Long id) {
        SedeResponseDto result = sedeService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<SedeTableDto>>> listSedes(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<SedeTableDto> result = sedeService.listSedes(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SedeResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSedeRequestDto request) {
        SedeResponseDto result = sedeService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Sede actualizada", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = sedeService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Sede activada" : "Sede inactivada";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

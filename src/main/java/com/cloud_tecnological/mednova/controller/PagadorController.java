package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.pagador.CreatePagadorRequestDto;
import com.cloud_tecnological.mednova.dto.pagador.PagadorResponseDto;
import com.cloud_tecnological.mednova.dto.pagador.PagadorTableDto;
import com.cloud_tecnological.mednova.dto.pagador.UpdatePagadorRequestDto;
import com.cloud_tecnological.mednova.services.PagadorService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payers")
public class PagadorController {

    private final PagadorService pagadorService;

    public PagadorController(PagadorService pagadorService) {
        this.pagadorService = pagadorService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PagadorResponseDto>> create(
            @Valid @RequestBody CreatePagadorRequestDto request) {
        PagadorResponseDto result = pagadorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Pagador creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PagadorResponseDto>> findById(@PathVariable Long id) {
        PagadorResponseDto result = pagadorService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @GetMapping("/third/{thirdPartyId}")
    public ResponseEntity<ApiResponse<PagadorResponseDto>> findByThirdParty(@PathVariable Long thirdPartyId) {
        PagadorResponseDto result = pagadorService.findByThirdParty(thirdPartyId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<PagadorTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<PagadorTableDto> result = pagadorService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PagadorResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePagadorRequestDto request) {
        PagadorResponseDto result = pagadorService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Pagador actualizado", false, result));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Boolean>> toggleActive(@PathVariable Long id) {
        Boolean active = pagadorService.toggleActive(id);
        String msg = Boolean.TRUE.equals(active) ? "Pagador activado" : "Pagador inactivado";
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), msg, false, active));
    }
}

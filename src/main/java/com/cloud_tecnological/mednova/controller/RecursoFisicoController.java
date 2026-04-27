package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.recursofisico.CreateRecursoFisicoRequestDto;
import com.cloud_tecnological.mednova.dto.recursofisico.RecursoFisicoResponseDto;
import com.cloud_tecnological.mednova.dto.recursofisico.RecursoFisicoTableDto;
import com.cloud_tecnological.mednova.dto.recursofisico.UpdateRecursoFisicoRequestDto;
import com.cloud_tecnological.mednova.services.RecursoFisicoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/physical-resources")
public class RecursoFisicoController {

    private final RecursoFisicoService recursoFisicoService;

    public RecursoFisicoController(RecursoFisicoService recursoFisicoService) {
        this.recursoFisicoService = recursoFisicoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecursoFisicoResponseDto>> create(
            @Valid @RequestBody CreateRecursoFisicoRequestDto request) {
        RecursoFisicoResponseDto result = recursoFisicoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Recurso físico creado exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecursoFisicoResponseDto>> findById(@PathVariable Long id) {
        RecursoFisicoResponseDto result = recursoFisicoService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecursoFisicoResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecursoFisicoRequestDto request) {
        RecursoFisicoResponseDto result = recursoFisicoService.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Recurso físico actualizado exitosamente", false, result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        Boolean result = recursoFisicoService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Recurso físico eliminado", false, result));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<RecursoFisicoTableDto>>> listActivos(
            @RequestBody PageableDto<?> request) {
        PageImpl<RecursoFisicoTableDto> result = recursoFisicoService.listActivos(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

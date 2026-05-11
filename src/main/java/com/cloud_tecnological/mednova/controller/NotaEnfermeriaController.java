package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.notaenfermeria.CreateNotaEnfermeriaRequestDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaFilterParams;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaResponseDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.NotaEnfermeriaTableDto;
import com.cloud_tecnological.mednova.dto.notaenfermeria.UpdateNotaEnfermeriaRequestDto;
import com.cloud_tecnological.mednova.services.NotaEnfermeriaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nursing-notes")
public class NotaEnfermeriaController {

    private final NotaEnfermeriaService service;

    public NotaEnfermeriaController(NotaEnfermeriaService service) {
        this.service = service;
    }

    // HU-FASE2-086: Crear nota de enfermería
    @PostMapping
    public ResponseEntity<ApiResponse<NotaEnfermeriaResponseDto>> create(
            @Valid @RequestBody CreateNotaEnfermeriaRequestDto request) {
        NotaEnfermeriaResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Nota de enfermería creada", false, result));
    }

    // HU-FASE2-086: Consultar por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotaEnfermeriaResponseDto>> findById(@PathVariable Long id) {
        NotaEnfermeriaResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-086: Listar / cronología (filtra por atención, paciente, tipo, turno, fechas)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<NotaEnfermeriaTableDto>>> list(
            @RequestBody PageableDto<NotaEnfermeriaFilterParams> pageable) {
        PageImpl<NotaEnfermeriaTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-086: Actualizar nota (solo si NO está firmada)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotaEnfermeriaResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotaEnfermeriaRequestDto request) {
        NotaEnfermeriaResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Nota de enfermería actualizada", false, result));
    }

    // HU-FASE2-086: Firmar nota (bloquea edición)
    @PatchMapping("/{id}/sign")
    public ResponseEntity<ApiResponse<NotaEnfermeriaResponseDto>> sign(@PathVariable Long id) {
        NotaEnfermeriaResponseDto result = service.sign(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Nota de enfermería firmada", false, result));
    }

    // HU-FASE2-086: Activar / inactivar (estado lógico)
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<NotaEnfermeriaResponseDto>> activate(@PathVariable Long id) {
        NotaEnfermeriaResponseDto result = service.setActive(id, true);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Nota activada", false, result));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<NotaEnfermeriaResponseDto>> deactivate(@PathVariable Long id) {
        NotaEnfermeriaResponseDto result = service.setActive(id, false);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Nota inactivada", false, result));
    }

    // HU-FASE2-086: Eliminación lógica (solo si NO está firmada)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Nota eliminada", false, null));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.glosa.CreateGlosaRequestDto;
import com.cloud_tecnological.mednova.dto.glosa.GlosaResponseDto;
import com.cloud_tecnological.mednova.dto.glosa.GlosaTableDto;
import com.cloud_tecnological.mednova.services.GlosaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/glosas")
public class GlosaController {

    private final GlosaService glosaService;

    public GlosaController(GlosaService glosaService) {
        this.glosaService = glosaService;
    }

    // HU-FASE2-062: Recibir glosa
    @PostMapping
    public ResponseEntity<ApiResponse<GlosaResponseDto>> create(
            @Valid @RequestBody CreateGlosaRequestDto request) {
        GlosaResponseDto result = glosaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Glosa registrada exitosamente", false, result));
    }

    // HU-FASE2-062: Consultar glosa por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GlosaResponseDto>> findById(@PathVariable Long id) {
        GlosaResponseDto result = glosaService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-062: Listar glosas
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<GlosaTableDto>>> list(
            @RequestBody PageableDto<?> pageable) {
        PageImpl<GlosaTableDto> result = glosaService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.catalogo.CatalogoResponseDto;
import com.cloud_tecnological.mednova.services.CatalogoService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/{catalogo}")
    public ResponseEntity<ApiResponse<List<CatalogoResponseDto>>> listar(
            @PathVariable String catalogo,
            @RequestParam(required = false) Boolean activo) {
        List<CatalogoResponseDto> resultados = catalogoService.findAll(catalogo, activo);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, resultados));
    }
}
package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.listaespera.*;
import com.cloud_tecnological.mednova.services.ListaEsperaCitaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wait-list")
public class ListaEsperaCitaController {

    private final ListaEsperaCitaService listaEsperaService;

    public ListaEsperaCitaController(ListaEsperaCitaService listaEsperaService) {
        this.listaEsperaService = listaEsperaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WaitListResponseDto>> create(
            @Valid @RequestBody CreateWaitListRequestDto request) {
        WaitListResponseDto result = listaEsperaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Paciente agregado a lista de espera", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WaitListResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, listaEsperaService.findById(id)));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<WaitListTableDto>>> listActivos(
            @RequestBody PageableDto<?> request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, listaEsperaService.listActivos(request)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Boolean>> cancel(@PathVariable Long id) {
        Boolean result = listaEsperaService.cancel(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Eliminado de lista de espera", false, result));
    }
}

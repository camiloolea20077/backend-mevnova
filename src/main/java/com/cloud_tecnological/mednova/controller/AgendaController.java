package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.agenda.*;
import com.cloud_tecnological.mednova.dto.traslado.BulkTransferRequestDto;
import com.cloud_tecnological.mednova.dto.traslado.BulkTransferResponseDto;
import com.cloud_tecnological.mednova.services.AgendaService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agendas")
public class AgendaController {

    private final AgendaService agendaService;

    public AgendaController(AgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AgendaResponseDto>> create(
            @Valid @RequestBody CreateAgendaRequestDto request) {
        AgendaResponseDto result = agendaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Agenda creada exitosamente", false, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgendaResponseDto>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, agendaService.findById(id)));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<AgendaTableDto>>> listActivos(
            @RequestBody PageableDto<?> request) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, agendaService.listActivos(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Agenda eliminada", false, agendaService.delete(id)));
    }

    @PostMapping("/bulk-transfer")
    public ResponseEntity<ApiResponse<BulkTransferResponseDto>> bulkTransfer(
            @Valid @RequestBody BulkTransferRequestDto request) {
        BulkTransferResponseDto result = agendaService.bulkTransfer(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Traslado masivo ejecutado", false, result));
    }
}

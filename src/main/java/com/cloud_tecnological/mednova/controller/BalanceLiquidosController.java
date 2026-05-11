package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceItemRequestDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceItemResponseDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosFilterParams;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosResponseDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.BalanceLiquidosTableDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.CreateBalanceLiquidosRequestDto;
import com.cloud_tecnological.mednova.dto.balanceliquidos.UpdateBalanceLiquidosRequestDto;
import com.cloud_tecnological.mednova.services.BalanceLiquidosService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fluid-balances")
public class BalanceLiquidosController {

    private final BalanceLiquidosService service;

    public BalanceLiquidosController(BalanceLiquidosService service) {
        this.service = service;
    }

    // HU-FASE2-088 CA1: Crear balance con items iniciales opcionales
    @PostMapping
    public ResponseEntity<ApiResponse<BalanceLiquidosResponseDto>> create(
            @Valid @RequestBody CreateBalanceLiquidosRequestDto request) {
        BalanceLiquidosResponseDto result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Balance de líquidos creado", false, result));
    }

    // HU-FASE2-088: Consultar por ID con sus detalles
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BalanceLiquidosResponseDto>> findById(@PathVariable Long id) {
        BalanceLiquidosResponseDto result = service.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-088: Listar balances paginado
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<BalanceLiquidosTableDto>>> list(
            @RequestBody PageableDto<BalanceLiquidosFilterParams> pageable) {
        PageImpl<BalanceLiquidosTableDto> result = service.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-088: Actualizar header del balance (observaciones, fecha, turno, profesional)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BalanceLiquidosResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBalanceLiquidosRequestDto request) {
        BalanceLiquidosResponseDto result = service.update(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Balance actualizado", false, result));
    }

    // HU-FASE2-088 CA1+CA2: Agregar detalle (recalcula totales y balance)
    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<BalanceItemResponseDto>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody BalanceItemRequestDto request) {
        BalanceItemResponseDto result = service.addItem(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "Detalle agregado al balance", false, result));
    }

    // HU-FASE2-088: Listar detalles del balance
    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<BalanceItemResponseDto>>> listItems(@PathVariable Long id) {
        List<BalanceItemResponseDto> result = service.listItems(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-088: Eliminar lógicamente un detalle (recalcula totales y balance)
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id, @PathVariable Long itemId) {
        service.deleteItem(id, itemId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Detalle eliminado", false, null));
    }

    // HU-FASE2-088: Eliminación lógica del balance completo
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Balance eliminado", false, null));
    }
}

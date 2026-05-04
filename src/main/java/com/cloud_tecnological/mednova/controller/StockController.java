package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.stock.StockFilterParams;
import com.cloud_tecnological.mednova.dto.stock.StockResponseDto;
import com.cloud_tecnological.mednova.dto.stock.StockTableDto;
import com.cloud_tecnological.mednova.services.StockService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    // HU-FASE2-071: Consultar fila de stock por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockResponseDto>> findById(@PathVariable Long id) {
        StockResponseDto result = stockService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-071: Listado FEFO (orden por vencimiento ASC) con filtros
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<StockTableDto>>> list(
            @RequestBody PageableDto<StockFilterParams> pageable) {
        PageImpl<StockTableDto> result = stockService.list(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

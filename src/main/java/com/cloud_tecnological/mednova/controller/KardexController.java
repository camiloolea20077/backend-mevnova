package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.kardex.ExpirationAlertDto;
import com.cloud_tecnological.mednova.dto.kardex.ExpirationAlertFilterParams;
import com.cloud_tecnological.mednova.dto.kardex.KardexFilterParams;
import com.cloud_tecnological.mednova.dto.kardex.KardexItemDto;
import com.cloud_tecnological.mednova.dto.kardex.LowStockAlertDto;
import com.cloud_tecnological.mednova.dto.kardex.LowStockAlertFilterParams;
import com.cloud_tecnological.mednova.services.KardexService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kardex")
public class KardexController {

    private final KardexService kardexService;

    public KardexController(KardexService kardexService) {
        this.kardexService = kardexService;
    }

    // HU-FASE2-078: Kardex (movimientos cronológicos con saldo acumulado por lote)
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<KardexItemDto>>> listKardex(
            @RequestBody PageableDto<KardexFilterParams> pageable) {
        PageImpl<KardexItemDto> result = kardexService.listKardex(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-078: Alertas de vencimiento (próximos a vencer o ya vencidos)
    @PostMapping("/expiration-alerts")
    public ResponseEntity<ApiResponse<PageImpl<ExpirationAlertDto>>> listExpirationAlerts(
            @RequestBody PageableDto<ExpirationAlertFilterParams> pageable) {
        PageImpl<ExpirationAlertDto> result = kardexService.listExpirationAlerts(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-FASE2-078: Alertas de stock mínimo (umbral por filtro)
    @PostMapping("/low-stock-alerts")
    public ResponseEntity<ApiResponse<PageImpl<LowStockAlertDto>>> listLowStockAlerts(
            @RequestBody PageableDto<LowStockAlertFilterParams> pageable) {
        PageImpl<LowStockAlertDto> result = kardexService.listLowStockAlerts(pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

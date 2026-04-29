package com.cloud_tecnological.mednova.controller;

import com.cloud_tecnological.mednova.dto.cartera.CarteraAgingDto;
import com.cloud_tecnological.mednova.dto.cartera.CarteraFiltroRequestDto;
import com.cloud_tecnological.mednova.dto.cuentaporcobrar.CuentaPorCobrarResponseDto;
import com.cloud_tecnological.mednova.dto.cuentaporcobrar.CuentaPorCobrarTableDto;
import com.cloud_tecnological.mednova.dto.cuentaporcobrar.RegistrarAbonoRequestDto;
import com.cloud_tecnological.mednova.services.CuentaPorCobrarService;
import com.cloud_tecnological.mednova.util.ApiResponse;
import com.cloud_tecnological.mednova.util.PageableDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/cuentas-por-cobrar")
public class CuentaPorCobrarController {

    private final CuentaPorCobrarService cxcService;

    public CuentaPorCobrarController(CuentaPorCobrarService cxcService) {
        this.cxcService = cxcService;
    }

    // HU-059: Consultar CxC por ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CuentaPorCobrarResponseDto>> findById(
            @PathVariable Long id) {
        CuentaPorCobrarResponseDto result = cxcService.findById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-059: Consultar CxC de una factura
    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<ApiResponse<CuentaPorCobrarResponseDto>> findByFactura(
            @PathVariable Long facturaId) {
        CuentaPorCobrarResponseDto result = cxcService.findByFactura(facturaId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-059: Listar CxC paginado
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<PageImpl<CuentaPorCobrarTableDto>>> listActive(
            @RequestBody PageableDto<?> request) {
        PageImpl<CuentaPorCobrarTableDto> result = cxcService.listActive(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }

    // HU-059: Registrar abono sobre una CxC
    @PostMapping("/{id}/abono")
    public ResponseEntity<ApiResponse<CuentaPorCobrarResponseDto>> registrarAbono(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarAbonoRequestDto dto) {
        CuentaPorCobrarResponseDto result = cxcService.registrarAbono(id, dto);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Abono registrado exitosamente", false, result));
    }

    // HU-060: Consultar cartera con aging por pagador
    @PostMapping("/cartera")
    public ResponseEntity<ApiResponse<List<CarteraAgingDto>>> consultarCartera(
            @RequestBody(required = false) CarteraFiltroRequestDto filtro) {
        List<CarteraAgingDto> result = cxcService.consultarCartera(filtro);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "OK", false, result));
    }
}

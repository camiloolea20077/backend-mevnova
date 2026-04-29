package com.cloud_tecnological.mednova.services;

import com.cloud_tecnological.mednova.dto.cuentaporcobrar.CuentaPorCobrarResponseDto;
import com.cloud_tecnological.mednova.dto.cuentaporcobrar.CuentaPorCobrarTableDto;
import com.cloud_tecnological.mednova.dto.cuentaporcobrar.RegistrarAbonoRequestDto;
import com.cloud_tecnological.mednova.entity.FacturaEntity;
import org.springframework.data.domain.PageImpl;

import com.cloud_tecnological.mednova.util.PageableDto;

public interface CuentaPorCobrarService {
    void crearDesdeFactura(FacturaEntity factura, Long usuarioId);
    CuentaPorCobrarResponseDto findById(Long id);
    CuentaPorCobrarResponseDto findByFactura(Long facturaId);
    PageImpl<CuentaPorCobrarTableDto> listActive(PageableDto<?> request);
    CuentaPorCobrarResponseDto registrarAbono(Long id, RegistrarAbonoRequestDto dto);
}

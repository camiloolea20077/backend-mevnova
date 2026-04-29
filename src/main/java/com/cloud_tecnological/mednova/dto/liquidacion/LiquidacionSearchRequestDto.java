package com.cloud_tecnological.mednova.dto.liquidacion;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LiquidacionSearchRequestDto {

    private Integer page;
    private Integer rows;
    private Long pacienteId;
    private Long facturaId;
    private String tipoCobro;
    private String estadoRecaudo;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
}

package com.cloud_tecnological.mednova.dto.cobrorule;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateCobroRuleRequestDto {

    private BigDecimal rangoIngresoDesde;
    private BigDecimal rangoIngresoHasta;
    private Long categoriaSisbenId;
    private BigDecimal porcentajeCobro;
    private BigDecimal valorFijo;
    private BigDecimal topeEvento;
    private BigDecimal topeAnual;
    private String unidadValor;
    private String observations;
    private Boolean active;
}

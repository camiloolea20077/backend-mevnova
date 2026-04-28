package com.cloud_tecnological.mednova.dto.cobrorule;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CobroRuleTableDto {

    private Long id;
    private Integer vigencia;
    private String regimen_nombre;
    private String tipo_cobro;
    private BigDecimal porcentaje_cobro;
    private BigDecimal valor_fijo;
    private BigDecimal tope_evento;
    private BigDecimal tope_anual;
    private Boolean activo;
    private Long total_rows;
}

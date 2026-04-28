package com.cloud_tecnological.mednova.dto.cobrorule;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CobroRuleResponseDto {

    private Long id;
    private Long empresa_id;
    private Integer vigencia;
    private Long regimen_id;
    private String regimen_nombre;
    private String tipo_cobro;
    private BigDecimal rango_ingreso_desde;
    private BigDecimal rango_ingreso_hasta;
    private Long categoria_sisben_id;
    private String sisben_nombre;
    private BigDecimal porcentaje_cobro;
    private BigDecimal valor_fijo;
    private BigDecimal tope_evento;
    private BigDecimal tope_anual;
    private String unidad_valor;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
}

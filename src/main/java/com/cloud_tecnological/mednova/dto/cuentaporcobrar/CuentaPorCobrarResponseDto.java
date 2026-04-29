package com.cloud_tecnological.mednova.dto.cuentaporcobrar;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CuentaPorCobrarResponseDto {

    private Long id;
    private Long empresa_id;
    private Long sede_id;
    private Long factura_id;
    private String invoice_number;
    private Long pagador_id;
    private String payer_name;
    private Long estado_cartera_id;
    private String estado_code;
    private String estado_name;
    private LocalDate fecha_causacion;
    private LocalDate fecha_vencimiento;
    private BigDecimal valor_inicial;
    private BigDecimal saldo_actual;
    private Integer dias_mora;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
}

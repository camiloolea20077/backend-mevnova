package com.cloud_tecnological.mednova.dto.cuentaporcobrar;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CuentaPorCobrarTableDto {

    private Long id;
    private String invoice_number;
    private String payer_name;
    private String estado_code;
    private String estado_name;
    private LocalDate fecha_causacion;
    private LocalDate fecha_vencimiento;
    private BigDecimal valor_inicial;
    private BigDecimal saldo_actual;
    private Integer dias_mora;
    private Boolean activo;
    private Long total_rows;
}

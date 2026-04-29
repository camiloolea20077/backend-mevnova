package com.cloud_tecnological.mednova.dto.cartera;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CarteraAgingDto {
    private Long pagador_id;
    private String payer_name;
    private String estado_code;
    private String estado_name;
    private Long total_cuentas;
    private BigDecimal saldo_total;
    private BigDecimal saldo_vigente;
    private BigDecimal saldo_0_30;
    private BigDecimal saldo_31_60;
    private BigDecimal saldo_61_90;
    private BigDecimal saldo_mayor_90;
}

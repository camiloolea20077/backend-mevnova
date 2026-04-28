package com.cloud_tecnological.mednova.dto.invoiceitem;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceItemTableDto {

    private Long id;
    private Long servicio_salud_id;
    private String service_name;
    private BigDecimal cantidad;
    private BigDecimal valor_unitario;
    private BigDecimal porcentaje_iva;
    private BigDecimal valor_iva;
    private BigDecimal valor_descuento;
    private BigDecimal valor_copago;
    private BigDecimal valor_cuota_moderadora;
    private BigDecimal subtotal;
    private BigDecimal total;
    private Long total_rows;
}

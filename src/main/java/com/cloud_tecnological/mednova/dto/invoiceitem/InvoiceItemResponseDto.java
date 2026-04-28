package com.cloud_tecnological.mednova.dto.invoiceitem;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceItemResponseDto {

    private Long id;
    private Long empresa_id;
    private Long factura_id;
    private Long servicio_salud_id;
    private String service_name;
    private Long atencion_id;
    private BigDecimal cantidad;
    private BigDecimal valor_unitario;
    private BigDecimal porcentaje_iva;
    private BigDecimal valor_iva;
    private BigDecimal valor_descuento;
    private BigDecimal valor_copago;
    private BigDecimal valor_cuota_moderadora;
    private BigDecimal subtotal;
    private BigDecimal total;
    private Long diagnostico_id;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
}

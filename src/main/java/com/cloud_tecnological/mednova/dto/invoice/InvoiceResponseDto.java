package com.cloud_tecnological.mednova.dto.invoice;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class InvoiceResponseDto {

    private Long id;
    private Long empresa_id;
    private Long sede_id;
    private String prefijo;
    private String numero;
    private Long admision_id;
    private String admission_number;
    private Long paciente_id;
    private String patient_name;
    private String document_number;
    private Long pagador_id;
    private String payer_name;
    private Long contrato_id;
    private Long estado_factura_id;
    private String status_code;
    private String status_name;
    private LocalDate fecha_factura;
    private LocalDate fecha_vencimiento;
    private BigDecimal subtotal;
    private BigDecimal total_iva;
    private BigDecimal total_descuento;
    private BigDecimal total_copago;
    private BigDecimal total_cuota_moderadora;
    private BigDecimal total_neto;
    private String observaciones;
    private Boolean activo;
    private LocalDateTime created_at;
}

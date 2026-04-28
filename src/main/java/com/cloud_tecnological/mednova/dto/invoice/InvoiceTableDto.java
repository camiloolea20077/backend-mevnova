package com.cloud_tecnological.mednova.dto.invoice;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class InvoiceTableDto {

    private Long id;
    private String numero;
    private String prefijo;
    private String patient_name;
    private String document_number;
    private String payer_name;
    private String status_code;
    private String status_name;
    private LocalDate fecha_factura;
    private BigDecimal total_neto;
    private Boolean activo;
    private Long total_rows;
}

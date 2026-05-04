package com.cloud_tecnological.mednova.dto.compra;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CompraTableDto {

    private Long id;
    private String purchaseNumber;
    private String supplierInvoiceNumber;
    private String warehouseName;
    private String supplierName;
    private LocalDate purchaseDate;
    private LocalDate receptionDate;
    private String state;
    private BigDecimal total;
    private Boolean active;
}

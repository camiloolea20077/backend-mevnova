package com.cloud_tecnological.mednova.dto.compra;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class CompraResponseDto {

    private Long id;
    private String purchaseNumber;
    private String supplierInvoiceNumber;

    private Long warehouseId;
    private String warehouseName;

    private Long supplierId;
    private String supplierCode;
    private String supplierName;

    private LocalDate purchaseDate;
    private LocalDate receptionDate;
    private String state;

    private BigDecimal subtotal;
    private BigDecimal totalVat;
    private BigDecimal totalDiscount;
    private BigDecimal total;

    private String supportUrl;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;

    private List<DetalleCompraResponseDto> items;
}

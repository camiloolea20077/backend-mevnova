package com.cloud_tecnological.mednova.dto.compra;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class DetalleCompraResponseDto {

    private Long id;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private Long batchId;
    private String batchNumber;
    private LocalDate expirationDate;
    private String invimaRegister;
    private BigDecimal quantity;
    private BigDecimal unitValue;
    private BigDecimal vatPercentage;
    private BigDecimal vatValue;
    private BigDecimal discountPercentage;
    private BigDecimal discountValue;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String observations;
}

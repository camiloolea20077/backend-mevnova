package com.cloud_tecnological.mednova.dto.lote;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class LoteResponseDto {

    private Long id;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expirationDate;
    private Integer daysUntilExpiration;
    private Boolean expired;
    private String invimaRegister;
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
    private BigDecimal totalStock;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

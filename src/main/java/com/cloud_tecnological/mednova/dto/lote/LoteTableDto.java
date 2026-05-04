package com.cloud_tecnological.mednova.dto.lote;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class LoteTableDto {

    private Long id;
    private String healthServiceCode;
    private String healthServiceName;
    private String batchNumber;
    private LocalDate expirationDate;
    private Integer daysUntilExpiration;
    private Boolean expired;
    private String invimaRegister;
    private String supplierName;
    private BigDecimal totalStock;
    private Boolean active;
}

package com.cloud_tecnological.mednova.dto.dispensacion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class DetalleDispensacionResponseDto {

    private Long id;
    private Long prescriptionDetailId;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private Long batchId;
    private String batchNumber;
    private LocalDate batchExpirationDate;
    private BigDecimal quantity;
    private BigDecimal unitValue;
    private String observations;
}

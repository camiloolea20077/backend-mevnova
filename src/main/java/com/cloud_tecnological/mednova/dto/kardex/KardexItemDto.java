package com.cloud_tecnological.mednova.dto.kardex;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class KardexItemDto {

    private Long movementId;
    private LocalDateTime movementDate;
    private String movementType;

    private Long batchId;
    private String batchNumber;
    private java.time.LocalDate expirationDate;

    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;

    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long destinationWarehouseId;
    private String destinationWarehouseName;

    /** Cantidad absoluta del movimiento. */
    private BigDecimal quantity;

    /** Cantidad con signo (positiva entrada, negativa salida). */
    private BigDecimal signedQuantity;

    /** Saldo acumulado del lote después del movimiento. */
    private BigDecimal runningBalance;

    private BigDecimal unitValue;
    private BigDecimal totalValue;

    private String referenceType;
    private Long referenceId;
    private String reason;
}

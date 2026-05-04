package com.cloud_tecnological.mednova.dto.devolucion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DevolucionItemResponseDto {

    private Long movementId;
    private Long dispensationId;
    private Long dispensationDetailId;
    private Long batchId;
    private String batchNumber;
    private LocalDate batchExpirationDate;
    private Long healthServiceId;
    private String healthServiceName;
    private BigDecimal quantity;
    private Long targetWarehouseId;
    private String targetWarehouseName;
    private String movementType;
    private String reason;
    private LocalDateTime movementDate;

    /** true si el lote estaba vencido y se aplicó BAJA_VENCIMIENTO en lugar de DEVOLUCION_PACIENTE. */
    private Boolean expiredBatchDiscarded;
}

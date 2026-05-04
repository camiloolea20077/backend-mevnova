package com.cloud_tecnological.mednova.dto.dispensacion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class DispensationSuggestionItemDto {

    private Long prescriptionDetailId;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private BigDecimal prescribedQuantity;
    private BigDecimal alreadyDispensedQuantity;
    private BigDecimal pendingQuantity;

    /** Sugerencia FEFO en la bodega indicada. Puede ser null si no hay stock disponible no vencido. */
    private Long suggestedBatchId;
    private String suggestedBatchNumber;
    private LocalDate suggestedExpirationDate;
    private BigDecimal suggestedAvailableQuantity;

    /** false si no hay stock para sugerir. */
    private Boolean canDispense;
}

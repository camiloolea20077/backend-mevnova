package com.cloud_tecnological.mednova.dto.solicitudmedicamento;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class DispatchSuggestionItemDto {

    private Long detailId;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private BigDecimal requestedQuantity;

    /** Sugerencia FEFO. Puede ser null si no hay stock disponible no vencido. */
    private Long suggestedBatchId;
    private String suggestedBatchNumber;
    private LocalDate suggestedExpirationDate;
    private BigDecimal suggestedAvailableQuantity;

    /** false si no hay stock para sugerir (item sería rechazado al despachar). */
    private Boolean canDispatch;
}

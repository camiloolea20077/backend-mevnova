package com.cloud_tecnological.mednova.dto.solicitudmedicamento;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DetalleSolicitudMedicamentoResponseDto {

    private Long id;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private BigDecimal requestedQuantity;
    private BigDecimal dispatchedQuantity;
    private String state;
    private String rejectionReason;
}

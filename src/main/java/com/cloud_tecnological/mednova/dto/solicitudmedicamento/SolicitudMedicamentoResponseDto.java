package com.cloud_tecnological.mednova.dto.solicitudmedicamento;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class SolicitudMedicamentoResponseDto {

    private Long id;
    private String requestNumber;

    private Long sourceWarehouseId;
    private String sourceWarehouseName;

    private Long destinationWarehouseId;
    private String destinationWarehouseName;

    private Long requestingProfessionalId;
    private String requestingProfessionalName;

    private String state;
    private String priority;

    private LocalDateTime requestDate;
    private LocalDateTime dispatchDate;

    private String reason;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;

    private List<DetalleSolicitudMedicamentoResponseDto> items;
}

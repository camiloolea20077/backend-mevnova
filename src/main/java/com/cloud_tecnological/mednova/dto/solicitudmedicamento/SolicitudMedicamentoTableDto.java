package com.cloud_tecnological.mednova.dto.solicitudmedicamento;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SolicitudMedicamentoTableDto {

    private Long id;
    private String requestNumber;
    private String sourceWarehouseName;
    private String destinationWarehouseName;
    private String requestingProfessionalName;
    private String priority;
    private String state;
    private LocalDateTime requestDate;
    private Boolean active;
}

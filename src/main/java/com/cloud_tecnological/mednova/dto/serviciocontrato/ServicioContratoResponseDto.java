package com.cloud_tecnological.mednova.dto.serviciocontrato;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ServicioContratoResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long contractId;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private Boolean requiresAuthorization;
    private Integer maxQuantity;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

package com.cloud_tecnological.mednova.dto.tarifario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DetalleTarifarioResponseDto {

    private Long id;
    private Long tarifarioId;
    private Long healthServiceId;
    private String healthServiceCode;
    private String healthServiceName;
    private BigDecimal value;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

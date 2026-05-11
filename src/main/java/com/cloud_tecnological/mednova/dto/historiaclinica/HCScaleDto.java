package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HCScaleDto {

    private Long scaleId;
    private Long encounterId;
    private String scaleType;
    private Integer totalScore;
    private String risk;
    private LocalDateTime appliedAt;
}

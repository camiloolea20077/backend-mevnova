package com.cloud_tecnological.mednova.dto.interconsulta;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InterconsultaTableDto {

    private Long id;
    private String number;
    private Long originEncounterId;
    private String requestingProfessionalName;
    private String destinationSpecialtyName;
    private String status;
    private String priority;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private Boolean active;
}

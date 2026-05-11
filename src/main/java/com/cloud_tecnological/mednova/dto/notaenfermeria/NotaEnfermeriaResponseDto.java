package com.cloud_tecnological.mednova.dto.notaenfermeria;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotaEnfermeriaResponseDto {

    private Long id;

    private Long encounterId;
    private Long patientId;

    private Long professionalId;
    private String professionalName;

    private String noteType;
    private String shift;
    private LocalDateTime noteDate;
    private String content;

    private Integer systolicBp;
    private Integer diastolicBp;
    private Integer heartRate;
    private Integer respiratoryRate;
    private BigDecimal temperature;
    private Integer oxygenSaturation;
    private BigDecimal glucometry;
    private Integer painEva;

    private Boolean signed;
    private LocalDateTime signedAt;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

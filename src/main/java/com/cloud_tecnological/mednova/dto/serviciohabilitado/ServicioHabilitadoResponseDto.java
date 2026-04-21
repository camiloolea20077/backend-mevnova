package com.cloud_tecnological.mednova.dto.serviciohabilitado;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ServicioHabilitadoResponseDto {

    private Long id;
    private Long sedeId;
    private String sedeName;
    private String serviceCode;
    private String serviceName;
    private String modality;
    private String complexity;
    private LocalDate enablementDate;
    private LocalDate expirationDate;
    private String resolution;
    private String observations;
    private Boolean active;
    private Boolean valid;
    private LocalDateTime createdAt;
}

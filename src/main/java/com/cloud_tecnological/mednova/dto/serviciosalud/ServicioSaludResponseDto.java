package com.cloud_tecnological.mednova.dto.serviciosalud;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ServicioSaludResponseDto {

    private Long id;
    private Long enterpriseId;
    private String internalCode;
    private String cupsCode;
    private String name;
    private String description;
    private Long healthServiceCategoryId;
    private String healthServiceCategoryName;
    private Long costCenterId;
    private String costCenterName;
    private String measureUnit;
    private Boolean requiresAuthorization;
    private Boolean requiresDiagnosis;
    private Boolean active;
    private LocalDateTime createdAt;
}

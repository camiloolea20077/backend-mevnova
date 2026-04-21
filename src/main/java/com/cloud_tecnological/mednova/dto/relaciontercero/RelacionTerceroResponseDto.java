package com.cloud_tecnological.mednova.dto.relaciontercero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RelacionTerceroResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long sourceThirdPartyId;
    private String sourceFullName;
    private Long destinationThirdPartyId;
    private String destinationFullName;
    private Long relationTypeId;
    private String relationTypeName;
    private Boolean isResponsible;
    private Boolean isEmergencyContact;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

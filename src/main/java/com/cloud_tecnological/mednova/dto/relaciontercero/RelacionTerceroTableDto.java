package com.cloud_tecnological.mednova.dto.relaciontercero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RelacionTerceroTableDto {

    private Long id;
    private String sourceFullName;
    private String destinationFullName;
    private String relationTypeName;
    private Boolean isResponsible;
    private Boolean isEmergencyContact;
    private Boolean active;
}

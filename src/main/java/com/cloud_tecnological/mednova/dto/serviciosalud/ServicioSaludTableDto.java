package com.cloud_tecnological.mednova.dto.serviciosalud;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServicioSaludTableDto {

    private Long id;
    private String internalCode;
    private String cupsCode;
    private String name;
    private String healthServiceCategoryName;
    private String costCenterName;
    private Boolean requiresAuthorization;
    private Boolean active;
}

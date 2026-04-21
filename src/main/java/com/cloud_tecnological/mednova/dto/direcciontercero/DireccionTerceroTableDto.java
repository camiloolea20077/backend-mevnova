package com.cloud_tecnological.mednova.dto.direcciontercero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DireccionTerceroTableDto {

    private Long id;
    private String addressType;
    private String municipalityName;
    private String address;
    private Boolean isPrincipal;
    private Boolean active;
}

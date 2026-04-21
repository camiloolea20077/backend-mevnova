package com.cloud_tecnological.mednova.dto.sede;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SedeTableDto {

    private Long id;
    private String code;
    private String name;
    private String municipalityName;
    private Boolean isPrincipal;
    private Boolean active;
}

package com.cloud_tecnological.mednova.dto.permiso;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PermisoDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String module;
    private Boolean active;
}

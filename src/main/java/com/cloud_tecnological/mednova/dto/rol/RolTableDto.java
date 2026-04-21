package com.cloud_tecnological.mednova.dto.rol;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RolTableDto {

    private Long id;
    private String code;
    private String name;
    private Integer permissionsCount;
    private Boolean active;
}

package com.cloud_tecnological.mednova.dto.centrocosto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CentroCostoTableDto {

    private Long id;
    private String code;
    private String name;
    private String parentName;
    private Boolean active;
}

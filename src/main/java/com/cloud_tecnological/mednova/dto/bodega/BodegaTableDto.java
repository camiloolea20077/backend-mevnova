package com.cloud_tecnological.mednova.dto.bodega;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BodegaTableDto {

    private Long id;
    private String branchName;
    private String code;
    private String name;
    private String warehouseType;
    private String responsibleName;
    private Boolean isPrincipal;
    private Boolean allowsDispense;
    private Boolean allowsReceive;
    private Boolean active;
}

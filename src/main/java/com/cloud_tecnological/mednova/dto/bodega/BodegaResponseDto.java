package com.cloud_tecnological.mednova.dto.bodega;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BodegaResponseDto {

    private Long id;
    private Long branchId;
    private String branchName;
    private String code;
    private String name;
    private String warehouseType;
    private Long responsibleId;
    private String responsibleName;
    private String physicalLocation;
    private Boolean isPrincipal;
    private Boolean allowsDispense;
    private Boolean allowsReceive;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

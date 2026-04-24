package com.cloud_tecnological.mednova.dto.centrocosto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class CentroCostoResponseDto {

    private Long id;
    private Long enterpriseId;
    private String code;
    private String name;
    private Long parentId;
    private String parentName;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
}

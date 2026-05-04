package com.cloud_tecnological.mednova.dto.traslado;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class TrasladoResponseDto {

    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long targetWarehouseId;
    private String targetWarehouseName;
    private LocalDateTime transferDate;
    private String reason;
    private List<TrasladoMovementResponseDto> movements;
}

package com.cloud_tecnological.mednova.dto.kardex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpirationAlertFilterParams {

    /** Umbral de días para considerar "próximo a vencer" (típicamente 30, 60, 90). */
    private Integer daysThreshold;

    /** Filtros opcionales. */
    private Long warehouseId;
    private Long branchId;
    private Long healthServiceId;

    /** Si true, retorna lotes ya vencidos con stock disponible. */
    private Boolean expired;
}

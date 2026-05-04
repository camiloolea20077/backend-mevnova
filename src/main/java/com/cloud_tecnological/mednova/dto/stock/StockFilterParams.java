package com.cloud_tecnological.mednova.dto.stock;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockFilterParams {

    private Long warehouseId;

    private Long healthServiceId;

    private Long batchId;

    /** 30, 60, 90 — lotes con vencimiento dentro de los próximos N días. */
    private Integer expiringInDays;

    /** true para incluir filas con cantidad_total = 0 (por defecto solo > 0). */
    private Boolean includeEmpty;
}

package com.cloud_tecnological.mednova.dto.lote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoteFilterParams {

    private Long healthServiceId;

    private Long supplierId;

    /** 30, 60, 90 — lotes que vencen en los próximos N días (no incluye vencidos). */
    private Integer expiringInDays;

    /** true para mostrar solo lotes vencidos. */
    private Boolean expired;

    /** true (por defecto) muestra solo lotes con stock total > 0. */
    private Boolean onlyWithStock;
}

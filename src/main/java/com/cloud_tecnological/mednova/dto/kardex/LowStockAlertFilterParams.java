package com.cloud_tecnological.mednova.dto.kardex;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LowStockAlertFilterParams {

    /**
     * Umbral mínimo de cantidad por bodega+servicio.
     * Como el esquema actual no almacena stock_minimo por servicio_salud,
     * el umbral se recibe como parámetro del filtro.
     */
    private BigDecimal minimumQuantity;

    /** Filtros opcionales. */
    private Long warehouseId;
    private Long branchId;
    private Long healthServiceId;
}

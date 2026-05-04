package com.cloud_tecnological.mednova.dto.kardex;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class KardexFilterParams {

    /** Lote a consultar. Si se envía, filtra por lote. */
    private Long batchId;

    /** Servicio de salud (medicamento/insumo). Si se envía sin batchId, agrupa todos los lotes del servicio. */
    private Long healthServiceId;

    /** Filtro opcional por bodega (origen o destino). */
    private Long warehouseId;

    /** Tipo de movimiento (ENTRADA_COMPRA, SALIDA_DISPENSACION, etc.) opcional. */
    private String movementType;

    /** Rango opcional de fechas. */
    private LocalDate fromDate;
    private LocalDate toDate;
}

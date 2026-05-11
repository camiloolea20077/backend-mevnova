package com.cloud_tecnological.mednova.dto.balanceliquidos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BalanceLiquidosFilterParams {

    /** Filtro por atención. */
    private Long encounterId;

    /** Filtro por paciente. */
    private Long patientId;

    /** Filtro por profesional. */
    private Long professionalId;

    /** Filtro por turno (MANANA, TARDE, NOCHE, DIA_24H). */
    private String shift;

    /** Rango de fecha del balance. */
    private LocalDate dateFrom;
    private LocalDate dateTo;

    /** Solo registros lógicamente activos. */
    private Boolean onlyActive;
}

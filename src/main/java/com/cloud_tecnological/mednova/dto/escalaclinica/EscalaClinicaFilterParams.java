package com.cloud_tecnological.mednova.dto.escalaclinica;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EscalaClinicaFilterParams {

    private Long encounterId;
    private Long patientId;
    private Long professionalId;

    /** Tipo de escala (CA2: evolución de una escala = filtrar por tipo + ORDER BY fecha_aplicacion ASC). */
    private String scaleType;

    private String risk;

    private LocalDate dateFrom;
    private LocalDate dateTo;

    private Boolean onlyActive;
}

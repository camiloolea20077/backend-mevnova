package com.cloud_tecnological.mednova.dto.plancuidados;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanCuidadosFilterParams {

    /** Filtro por atención (consola hospitalización). */
    private Long encounterId;

    /** Filtro por paciente. */
    private Long patientId;

    /** Filtro por profesional. */
    private Long professionalId;

    /** Filtro por estado (ACTIVO, CUMPLIDO, MODIFICADO, SUSPENDIDO). */
    private String status;

    /** Filtra solo planes ACTIVO. */
    private Boolean onlyActiveStatus;

    /** Filtra por estado lógico activo (no soft-deleted). */
    private Boolean onlyActive;
}

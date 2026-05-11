package com.cloud_tecnological.mednova.dto.interconsulta;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InterconsultaFilterParams {

    private Long originEncounterId;
    private Long requestingProfessionalId;
    private Long respondingProfessionalId;
    private Long destinationSpecialtyId;

    /** PENDIENTE, EN_PROCESO, RESPONDIDA, ANULADA. */
    private String status;

    /** NORMAL, URGENTE, VITAL. */
    private String priority;

    /** Solo pendientes (PENDIENTE o EN_PROCESO). */
    private Boolean onlyPending;

    /** Si true, busca cross-sede dentro de la misma empresa (bandeja del especialista). */
    private Boolean crossSede;

    private LocalDate requestedFrom;
    private LocalDate requestedTo;

    private Boolean onlyActive;
}

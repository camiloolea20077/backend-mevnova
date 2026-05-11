package com.cloud_tecnological.mednova.dto.adjuntoclinico;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdjuntoClinicoFilterParams {

    private Long patientId;
    private Long encounterId;

    /** Filtro por tipo de documento. */
    private String documentType;

    private Long uploadingProfessionalId;

    /** Filtro por confidencial. Solo aplica si el usuario tiene permiso para verlos. */
    private Boolean confidential;

    /** Rango de fecha del documento. */
    private LocalDate documentDateFrom;
    private LocalDate documentDateTo;

    private Boolean onlyActive;
}

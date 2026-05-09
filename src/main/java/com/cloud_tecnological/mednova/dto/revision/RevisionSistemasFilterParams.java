package com.cloud_tecnological.mednova.dto.revision;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevisionSistemasFilterParams {

    /** Atención sobre la cual se aplicó la revisión (filtro principal). */
    private Long encounterId;

    /** Filtro por paciente (se hace join con atencion). */
    private Long patientId;

    /** Filtro por sistema (CARDIOVASCULAR, RESPIRATORIO, ...). */
    private String system;

    /** true = sólo registros sin alteración; false = sólo con hallazgos. */
    private Boolean withoutAlteration;

    /** Filtra por estado lógico activo (no soft-deleted). */
    private Boolean onlyActive;
}

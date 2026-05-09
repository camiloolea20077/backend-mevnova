package com.cloud_tecnological.mednova.dto.medicacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicacionHabitualFilterParams {

    /** Paciente requerido para acotar el listado a su HC. */
    private Long patientId;

    /** Filtra solo los medicamentos que aún toma (es_activo = true). */
    private Boolean onlyCurrentlyTaking;

    /** Filtra por servicio de salud (medicamento homologado). */
    private Long healthServiceId;

    /** Filtra por estado lógico activo (no soft-deleted). */
    private Boolean onlyActive;
}

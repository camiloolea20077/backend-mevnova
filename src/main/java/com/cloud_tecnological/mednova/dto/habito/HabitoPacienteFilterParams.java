package com.cloud_tecnological.mednova.dto.habito;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HabitoPacienteFilterParams {

    /** Paciente requerido para acotar el listado a su HC. */
    private Long patientId;

    /** Filtra por tipo de hábito (ALCOHOL, TABACO, ...). */
    private String habitType;

    /** Filtra por estado (ACTIVO, EX_CONSUMIDOR, NUNCA, OCASIONAL). */
    private String status;

    /** Solo hábitos en estado ACTIVO. */
    private Boolean onlyCurrentlyActive;

    /** Filtra por estado lógico activo (no soft-deleted). */
    private Boolean onlyActive;
}

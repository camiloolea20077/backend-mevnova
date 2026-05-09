package com.cloud_tecnological.mednova.dto.vacuna;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VacunaPacienteFilterParams {

    /** Paciente requerido para acotar el listado a su HC. */
    private Long patientId;

    /** Filtra por código de vacuna PAI (DPT, BCG, ...). */
    private String vaccineCode;

    /** Próxima dosis pendiente desde esta fecha (inclusive). */
    private LocalDate nextDoseFrom;

    /** Próxima dosis pendiente hasta esta fecha (inclusive). */
    private LocalDate nextDoseTo;

    /** Filtra por estado lógico activo (no soft-deleted). */
    private Boolean onlyActive;
}

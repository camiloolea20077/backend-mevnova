package com.cloud_tecnological.mednova.dto.antecedente;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AntecedenteFamiliarFilterParams {

    /** Paciente requerido para acotar el listado a su HC. */
    private Long patientId;

    /** Filtra por parentesco (coincidencia exacta, ej: MADRE, PADRE). */
    private String kinship;

    /** Filtra solo familiares fallecidos. */
    private Boolean onlyDeceased;

    /** Filtra por estado lógico activo (no soft-deleted). */
    private Boolean onlyActive;
}

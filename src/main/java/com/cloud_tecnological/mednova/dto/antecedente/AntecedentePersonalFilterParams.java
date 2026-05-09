package com.cloud_tecnological.mednova.dto.antecedente;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AntecedentePersonalFilterParams {

    /** Paciente requerido para acotar el listado a su HC. */
    private Long patientId;

    private Long antecedentTypeId;

    /** Filtra solo alérgicos (tipo_antecedente.codigo = ALERGICO). */
    private Boolean onlyAllergies;

    /** Filtra por es_activo (vigentes vs históricos). */
    private Boolean onlyActiveCondition;

    /** Filtra por estado lógico activo (no soft-deleted). */
    private Boolean onlyActive;
}

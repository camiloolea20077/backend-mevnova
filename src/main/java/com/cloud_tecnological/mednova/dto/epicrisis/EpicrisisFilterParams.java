package com.cloud_tecnological.mednova.dto.epicrisis;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EpicrisisFilterParams {

    private Long admissionId;
    private Long patientId;
    private Long professionalId;

    /** Filtra por estado de firma. */
    private Boolean signed;

    /** Rango de fecha de egreso. */
    private LocalDate dischargeFrom;
    private LocalDate dischargeTo;

    private Boolean onlyActive;
}

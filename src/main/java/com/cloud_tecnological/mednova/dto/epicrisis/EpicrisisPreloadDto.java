package com.cloud_tecnological.mednova.dto.epicrisis;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CA2: datos precargados desde la admisión para inicializar la epicrisis.
 */
@Getter
@Setter
@Builder
public class EpicrisisPreloadDto {

    private Long admissionId;
    private String admissionNumber;
    private Long patientId;
    private String patientName;

    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;

    private String admissionReason;

    /** Códigos CIE-10 + descripciones registrados durante la admisión. */
    private List<String> admissionDiagnoses;

    /** Procedimientos realizados (servicios facturados / órdenes ejecutadas). */
    private List<String> performedProcedures;
}

package com.cloud_tecnological.mednova.dto.epicrisis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateEpicrisisRequestDto {

    @NotNull(message = "admissionId es obligatorio")
    private Long admissionId;

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    /** Si no se envía, se toma de admision.fecha_egreso. */
    private LocalDateTime dischargeDate;

    @NotBlank(message = "admissionReason es obligatorio")
    private String admissionReason;

    @NotBlank(message = "admissionDiagnosis es obligatorio")
    private String admissionDiagnosis;

    @NotBlank(message = "dischargeDiagnosis es obligatorio")
    private String dischargeDiagnosis;

    private String proceduresPerformed;

    @NotBlank(message = "evolutionSummary es obligatorio")
    private String evolutionSummary;

    private String complications;

    @NotBlank(message = "followUpPlan es obligatorio")
    private String followUpPlan;

    private String dischargeMedications;

    @NotBlank(message = "recommendations es obligatorio")
    private String recommendations;

    private String dietInstructions;

    private String activityInstructions;

    private LocalDate nextControlDate;

    /** Si true, la epicrisis se crea ya firmada. */
    private Boolean signOnCreate;

    /** URL del PDF generado (CA4). */
    private String pdfUrl;
}

package com.cloud_tecnological.mednova.dto.epicrisis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Actualización antes de firmar. Una vez firmada la epicrisis no se edita (CA3).
 */
@Getter
@Setter
public class UpdateEpicrisisRequestDto {

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

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

    private String pdfUrl;
}

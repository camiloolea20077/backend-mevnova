package com.cloud_tecnological.mednova.dto.epicrisis;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EpicrisisResponseDto {

    private Long id;

    private Long admissionId;
    private String admissionNumber;
    private Long patientId;

    private Long professionalId;
    private String professionalName;

    private LocalDateTime dischargeDate;

    private String admissionReason;
    private String admissionDiagnosis;
    private String dischargeDiagnosis;
    private String proceduresPerformed;
    private String evolutionSummary;
    private String complications;
    private String followUpPlan;
    private String dischargeMedications;
    private String recommendations;
    private String dietInstructions;
    private String activityInstructions;
    private LocalDate nextControlDate;

    private Boolean signed;
    private LocalDateTime signedAt;
    private String pdfUrl;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

package com.cloud_tecnological.mednova.dto.epicrisis;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EpicrisisTableDto {

    private Long id;
    private Long admissionId;
    private String admissionNumber;
    private Long patientId;
    private String professionalName;
    private LocalDateTime dischargeDate;
    private Boolean signed;
    private Boolean active;
}

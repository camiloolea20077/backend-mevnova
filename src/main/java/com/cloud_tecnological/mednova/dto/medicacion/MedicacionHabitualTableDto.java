package com.cloud_tecnological.mednova.dto.medicacion;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class MedicacionHabitualTableDto {

    private Long id;
    private Long patientId;
    private String medicationName;
    private String dose;
    private String administrationRouteName;
    private String doseFrequencyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrentlyTaking;
    private String prescribingProfessional;
    private Boolean active;
}

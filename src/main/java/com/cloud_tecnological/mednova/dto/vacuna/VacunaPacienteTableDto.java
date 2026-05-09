package com.cloud_tecnological.mednova.dto.vacuna;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class VacunaPacienteTableDto {

    private Long id;
    private Long patientId;
    private String vaccineName;
    private String vaccineCode;
    private Integer doseNumber;
    private Integer totalSchemeDoses;
    private LocalDate applicationDate;
    private LocalDate nextDoseDate;
    private String laboratory;
    private String administrationRouteName;
    private String applyingInstitution;
    private Boolean active;
}

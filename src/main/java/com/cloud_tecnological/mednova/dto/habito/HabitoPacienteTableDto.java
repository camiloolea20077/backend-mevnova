package com.cloud_tecnological.mednova.dto.habito;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class HabitoPacienteTableDto {

    private Long id;
    private Long patientId;
    private String habitType;
    private String description;
    private String frequency;
    private String quantity;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}

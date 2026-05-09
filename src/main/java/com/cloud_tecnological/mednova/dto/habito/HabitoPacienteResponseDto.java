package com.cloud_tecnological.mednova.dto.habito;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HabitoPacienteResponseDto {

    private Long id;

    private Long patientId;

    private String habitType;

    private String description;
    private String frequency;
    private String quantity;
    private String consumptionTime;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;
    private String observations;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

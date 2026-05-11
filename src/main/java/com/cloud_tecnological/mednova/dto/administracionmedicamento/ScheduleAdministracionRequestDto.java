package com.cloud_tecnological.mednova.dto.administracionmedicamento;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleAdministracionRequestDto {

    @NotNull(message = "prescriptionDetailId es obligatorio")
    private Long prescriptionDetailId;

    @NotNull(message = "encounterId es obligatorio")
    private Long encounterId;

    @NotNull(message = "patientId es obligatorio")
    private Long patientId;

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    @NotNull(message = "firstDoseAt es obligatorio")
    private LocalDateTime firstDoseAt;

    @NotNull(message = "intervalHours es obligatorio")
    @Min(value = 1, message = "intervalHours mínimo 1")
    @Max(value = 168, message = "intervalHours máximo 168 (1 semana)")
    private Integer intervalHours;

    @NotNull(message = "totalDoses es obligatorio")
    @Min(value = 1, message = "totalDoses mínimo 1")
    @Max(value = 365, message = "totalDoses máximo 365")
    private Integer totalDoses;
}

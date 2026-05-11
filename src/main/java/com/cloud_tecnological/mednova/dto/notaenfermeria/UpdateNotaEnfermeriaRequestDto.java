package com.cloud_tecnological.mednova.dto.notaenfermeria;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateNotaEnfermeriaRequestDto {

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    @NotBlank(message = "noteType es obligatorio")
    @Pattern(
            regexp = "INGRESO|EVOLUCION|NOVEDAD|ENTREGA_TURNO|POST_PROCEDIMIENTO|EDUCACION|PRE_QUIRURGICA|POST_QUIRURGICA",
            message = "noteType debe ser INGRESO, EVOLUCION, NOVEDAD, ENTREGA_TURNO, POST_PROCEDIMIENTO, EDUCACION, PRE_QUIRURGICA o POST_QUIRURGICA"
    )
    private String noteType;

    @Pattern(
            regexp = "MANANA|TARDE|NOCHE",
            message = "shift debe ser MANANA, TARDE o NOCHE"
    )
    private String shift;

    private LocalDateTime noteDate;

    @NotBlank(message = "content es obligatorio")
    private String content;

    @Min(value = 0, message = "systolicBp mínimo 0")
    @Max(value = 300, message = "systolicBp máximo 300")
    private Integer systolicBp;

    @Min(value = 0, message = "diastolicBp mínimo 0")
    @Max(value = 200, message = "diastolicBp máximo 200")
    private Integer diastolicBp;

    @Min(value = 0, message = "heartRate mínimo 0")
    @Max(value = 300, message = "heartRate máximo 300")
    private Integer heartRate;

    @Min(value = 0, message = "respiratoryRate mínimo 0")
    private Integer respiratoryRate;

    private BigDecimal temperature;

    @Min(value = 0, message = "oxygenSaturation mínimo 0")
    @Max(value = 100, message = "oxygenSaturation máximo 100")
    private Integer oxygenSaturation;

    private BigDecimal glucometry;

    @Min(value = 0, message = "painEva mínimo 0")
    @Max(value = 10, message = "painEva máximo 10")
    private Integer painEva;
}

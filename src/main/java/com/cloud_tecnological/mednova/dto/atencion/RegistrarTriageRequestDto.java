package com.cloud_tecnological.mednova.dto.atencion;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class RegistrarTriageRequestDto {

    @NotNull(message = "El nivel de triage es obligatorio")
    @Pattern(regexp = "^[I]{1,3}$|^IV$|^V$", message = "El nivel de triage debe ser I, II, III, IV o V")
    private String triageLevel;

    @NotBlank(message = "El motivo de consulta es obligatorio")
    private String chiefComplaint;

    @Min(value = 0, message = "La tensión sistólica no puede ser negativa")
    @Max(value = 300, message = "La tensión sistólica excede el rango válido")
    private Integer systolicPressure;

    @Min(value = 0, message = "La tensión diastólica no puede ser negativa")
    @Max(value = 200, message = "La tensión diastólica excede el rango válido")
    private Integer diastolicPressure;

    @Min(value = 0, message = "La frecuencia cardíaca no puede ser negativa")
    @Max(value = 300, message = "La frecuencia cardíaca excede el rango válido")
    private Integer heartRate;

    @Min(value = 0, message = "La frecuencia respiratoria no puede ser negativa")
    @Max(value = 100, message = "La frecuencia respiratoria excede el rango válido")
    private Integer respiratoryRate;

    private BigDecimal temperature;

    @Min(value = 0, message = "La saturación de oxígeno no puede ser negativa")
    @Max(value = 100, message = "La saturación de oxígeno no puede superar 100%")
    private Integer oxygenSaturation;

    private BigDecimal weight;

    private BigDecimal height;

    private BigDecimal glucometry;

    private String observations;
}

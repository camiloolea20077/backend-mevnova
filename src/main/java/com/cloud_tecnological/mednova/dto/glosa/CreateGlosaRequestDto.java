package com.cloud_tecnological.mednova.dto.glosa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateGlosaRequestDto {

    @NotNull(message = "La factura es obligatoria")
    private Long invoiceId;

    @NotBlank(message = "El número de oficio del pagador es obligatorio")
    @Size(max = 50, message = "El número de oficio no puede superar 50 caracteres")
    private String payerOfficeNumber;

    @NotNull(message = "La fecha de oficio es obligatoria")
    private LocalDate officeDate;

    @NotNull(message = "La fecha de notificación es obligatoria")
    private LocalDate notificationDate;

    @NotNull(message = "El valor glosado es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor glosado debe ser mayor que cero")
    private BigDecimal totalGlossedValue;

    @Size(max = 500, message = "La URL de soporte no puede superar 500 caracteres")
    private String officeUrl;

    private String observations;
}

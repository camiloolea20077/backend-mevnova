package com.cloud_tecnological.mednova.dto.compra;

import jakarta.validation.constraints.DecimalMax;
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
public class CreateDetalleCompraRequestDto {

    @NotNull(message = "El servicio de salud es obligatorio")
    private Long healthServiceId;

    @NotBlank(message = "El número de lote es obligatorio")
    @Size(max = 50, message = "El número de lote no puede superar 50 caracteres")
    private String batchNumber;

    private LocalDate manufacturingDate;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate expirationDate;

    @Size(max = 50, message = "El registro INVIMA no puede superar 50 caracteres")
    private String invimaRegister;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor que cero")
    private BigDecimal quantity;

    @NotNull(message = "El valor unitario es obligatorio")
    @DecimalMin(value = "0.00", message = "El valor unitario no puede ser negativo")
    private BigDecimal unitValue;

    @DecimalMin(value = "0.00", message = "El porcentaje de IVA no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El porcentaje de IVA no puede superar 100")
    private BigDecimal vatPercentage;

    @DecimalMin(value = "0.00", message = "El porcentaje de descuento no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El porcentaje de descuento no puede superar 100")
    private BigDecimal discountPercentage;

    @Size(max = 300, message = "Las observaciones no pueden superar 300 caracteres")
    private String observations;
}

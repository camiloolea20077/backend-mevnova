package com.cloud_tecnological.mednova.dto.invoiceitem;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateInvoiceItemRequestDto {

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal quantity;

    @NotNull(message = "El valor unitario es obligatorio")
    @DecimalMin(value = "0", message = "El valor unitario no puede ser negativo")
    private BigDecimal unitValue;

    private BigDecimal ivaPercentage;

    private BigDecimal discountValue;

    private Long diagnosisId;

    private String observations;
}

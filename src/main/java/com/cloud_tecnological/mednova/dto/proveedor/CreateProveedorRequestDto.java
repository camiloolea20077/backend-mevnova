package com.cloud_tecnological.mednova.dto.proveedor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProveedorRequestDto {

    @NotNull(message = "El tercero es obligatorio")
    private Long thirdPartyId;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 30, message = "El código no puede superar 30 caracteres")
    private String code;

    @Size(max = 30, message = "La cuenta contable no puede superar 30 caracteres")
    private String accountingAccount;

    @Min(value = 0, message = "El plazo de pago no puede ser negativo")
    private Integer paymentTermDays;

    @DecimalMin(value = "0.00", message = "El descuento no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El descuento no puede superar 100")
    private BigDecimal earlyPaymentDiscount;

    private Boolean requiresPurchaseOrder;

    private String observations;
}

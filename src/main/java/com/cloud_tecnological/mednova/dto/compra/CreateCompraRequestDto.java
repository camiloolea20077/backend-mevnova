package com.cloud_tecnological.mednova.dto.compra;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateCompraRequestDto {

    @NotNull(message = "La bodega de destino es obligatoria")
    private Long warehouseId;

    @NotNull(message = "El proveedor es obligatorio")
    private Long supplierId;

    @Size(max = 50, message = "El número de factura no puede superar 50 caracteres")
    private String supplierInvoiceNumber;

    @NotNull(message = "La fecha de compra es obligatoria")
    private LocalDate purchaseDate;

    private LocalDate receptionDate;

    @Size(max = 500, message = "La URL del soporte no puede superar 500 caracteres")
    private String supportUrl;

    private String observations;

    @NotEmpty(message = "Debe registrar al menos un ítem")
    @Valid
    private List<CreateDetalleCompraRequestDto> items;
}

package com.cloud_tecnological.mednova.dto.pagador;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePagadorRequestDto {

    @NotNull(message = "El tipo de pagador es obligatorio")
    private Long payerTypeId;

    private Long clientTypeId;

    @Size(max = 20, message = "El código EPS no puede superar 20 caracteres")
    private String epsCode;

    @Size(max = 20, message = "El código administradora no puede superar 20 caracteres")
    private String administratorCode;

    @Min(value = 1, message = "Los días de radicación deben ser mayor a 0")
    private Integer filingDays;

    @Min(value = 1, message = "Los días de respuesta glosa deben ser mayor a 0")
    private Integer glossaResponseDays;
}

package com.cloud_tecnological.mednova.dto.ajuste;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateAjusteInventarioRequestDto {

    @NotNull(message = "La bodega es obligatoria")
    private Long warehouseId;

    @NotBlank(message = "El tipo de ajuste es obligatorio")
    @Pattern(
            regexp = "FISICO|DETERIORO|VENCIMIENTO|ERROR_CAPTURA|PERDIDA|OTRO",
            message = "El tipo de ajuste no es válido"
    )
    private String adjustmentType;

    @NotNull(message = "La fecha del ajuste es obligatoria")
    private LocalDate adjustmentDate;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 1000, message = "El motivo no puede superar 1000 caracteres")
    private String reason;

    @NotEmpty(message = "Debe registrar al menos un ítem")
    @Valid
    private List<CreateDetalleAjusteRequestDto> items;
}

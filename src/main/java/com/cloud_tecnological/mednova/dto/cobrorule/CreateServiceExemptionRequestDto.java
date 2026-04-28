package com.cloud_tecnological.mednova.dto.cobrorule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateServiceExemptionRequestDto {

    @NotNull(message = "El servicio de salud es obligatorio")
    private Long healthServiceId;

    @NotBlank(message = "El tipo de cobro es obligatorio")
    @Pattern(regexp = "CUOTA_MODERADORA|COPAGO", message = "Tipo de cobro inválido")
    private String tipoCobro;

    @NotBlank(message = "El motivo de exención es obligatorio")
    private String motivoExencion;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate vigenciaDesde;

    private LocalDate vigenciaHasta;
}

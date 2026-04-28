package com.cloud_tecnological.mednova.dto.cobrorule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateCobroRuleRequestDto {

    @NotNull(message = "La vigencia es obligatoria")
    private Integer vigencia;

    @NotNull(message = "El régimen es obligatorio")
    private Long regimenId;

    @NotBlank(message = "El tipo de cobro es obligatorio")
    @Pattern(regexp = "CUOTA_MODERADORA|COPAGO", message = "Tipo de cobro inválido")
    private String tipoCobro;

    private BigDecimal rangoIngresoDesde;
    private BigDecimal rangoIngresoHasta;
    private Long categoriaSisbenId;
    private BigDecimal porcentajeCobro;
    private BigDecimal valorFijo;
    private BigDecimal topeEvento;
    private BigDecimal topeAnual;
    private String unidadValor;
    private String observations;
}

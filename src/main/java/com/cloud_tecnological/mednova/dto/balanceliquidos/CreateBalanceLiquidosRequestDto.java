package com.cloud_tecnological.mednova.dto.balanceliquidos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CreateBalanceLiquidosRequestDto {

    @NotNull(message = "encounterId es obligatorio")
    private Long encounterId;

    @NotNull(message = "patientId es obligatorio")
    private Long patientId;

    @NotNull(message = "professionalId es obligatorio")
    private Long professionalId;

    private LocalDate balanceDate;

    @Pattern(regexp = "MANANA|TARDE|NOCHE|DIA_24H",
            message = "shift debe ser MANANA, TARDE, NOCHE o DIA_24H")
    private String shift;

    private String observations;

    @Valid
    private List<BalanceItemRequestDto> items;
}

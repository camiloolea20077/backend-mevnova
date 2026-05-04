package com.cloud_tecnological.mednova.dto.solicitudmedicamento;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DispatchSolicitudRequestDto {

    @NotEmpty(message = "Debe registrar al menos un ítem a despachar")
    @Valid
    private List<DispatchSolicitudItemRequestDto> items;
}

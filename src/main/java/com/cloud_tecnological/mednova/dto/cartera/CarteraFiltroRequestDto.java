package com.cloud_tecnological.mednova.dto.cartera;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarteraFiltroRequestDto {
    private Long pagadorId;
    private String estadoCode;
    private Boolean soloConSaldo;
}

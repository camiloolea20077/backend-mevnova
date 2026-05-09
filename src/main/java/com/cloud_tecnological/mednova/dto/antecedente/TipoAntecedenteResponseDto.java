package com.cloud_tecnological.mednova.dto.antecedente;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TipoAntecedenteResponseDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean active;
}

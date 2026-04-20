package com.cloud_tecnological.mednova.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SedeDto {
    private Long id;
    private String codigo;
    private String nombre;
}

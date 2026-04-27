package com.cloud_tecnological.mednova.dto.diagnostico;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogoDiagnosticoSearchDto {
    private Long id;
    private String codigo;
    private String nombre;
    private String capitulo;
    private Boolean activo;
}

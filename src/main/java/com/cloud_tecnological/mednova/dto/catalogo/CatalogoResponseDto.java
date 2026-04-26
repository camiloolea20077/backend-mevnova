package com.cloud_tecnological.mednova.dto.catalogo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoResponseDto {

    private Long id;

    private String codigo;

    private String nombre;

    private String descripcion;

    private Boolean activo;

    @JsonProperty("label")
    private String label;
}
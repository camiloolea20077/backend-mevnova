package com.cloud_tecnological.mednova.dto.recursofisico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRecursoFisicoRequestDto {

    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    @NotBlank
    @Pattern(regexp = "CONSULTORIO|SALA|EQUIPO|CAMA|BOX|UCI",
             message = "tipoRecurso debe ser: CONSULTORIO, SALA, EQUIPO, CAMA, BOX o UCI")
    private String tipoRecurso;

    private String ubicacion;
    private String descripcion;
    private Boolean activo;
}

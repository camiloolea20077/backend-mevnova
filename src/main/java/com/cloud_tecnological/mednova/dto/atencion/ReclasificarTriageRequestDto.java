package com.cloud_tecnological.mednova.dto.atencion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReclasificarTriageRequestDto {

    @NotNull(message = "El nuevo nivel de triage es obligatorio")
    @Pattern(regexp = "^[I]{1,3}$|^IV$|^V$", message = "El nivel de triage debe ser I, II, III, IV o V")
    private String newTriageLevel;

    @NotBlank(message = "La justificación es obligatoria")
    private String justification;
}

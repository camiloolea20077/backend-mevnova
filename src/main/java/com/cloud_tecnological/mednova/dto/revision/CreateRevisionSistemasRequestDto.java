package com.cloud_tecnological.mednova.dto.revision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRevisionSistemasRequestDto {

    @NotNull(message = "encounterId es obligatorio")
    private Long encounterId;

    @NotBlank(message = "system es obligatorio")
    @Pattern(
            regexp = "CARDIOVASCULAR|RESPIRATORIO|GASTROINTESTINAL|GENITOURINARIO|"
                    + "NEUROLOGICO|MUSCULOESQUELETICO|PIEL_FANERAS|HEMATOLOGICO|"
                    + "ENDOCRINO|OFTALMOLOGICO|OTORRINO|PSIQUIATRICO|GENERAL",
            message = "system debe ser uno de los sistemas válidos"
    )
    private String system;

    /** true = sin alteración (default). false = se reportan hallazgos. */
    private Boolean withoutAlteration;

    private String findings;
}

package com.cloud_tecnological.mednova.dto.adjuntoclinico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Actualización de metadatos del adjunto.
 * No se cambia url_archivo desde aquí; eso requiere nuevo upload.
 */
@Getter
@Setter
public class UpdateAdjuntoClinicoRequestDto {

    @NotBlank(message = "documentType es obligatorio")
    @Pattern(
            regexp = "RESULTADO_LABORATORIO|IMAGEN_DIAGNOSTICA|REPORTE_PATOLOGIA|CONSENTIMIENTO|EXAMEN_EXTERNO|FOTO_CLINICA|ECG|OTRO",
            message = "documentType debe ser un valor válido del catálogo"
    )
    private String documentType;

    @NotBlank(message = "fileName es obligatorio")
    @Size(max = 300)
    private String fileName;

    @Size(max = 500)
    private String description;

    private LocalDate documentDate;

    private Boolean isConfidential;
}

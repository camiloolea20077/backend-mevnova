package com.cloud_tecnological.mednova.dto.adjuntoclinico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateAdjuntoClinicoRequestDto {

    @NotNull(message = "patientId es obligatorio")
    private Long patientId;

    /** Opcional: atención específica relacionada al documento. */
    private Long encounterId;

    @NotBlank(message = "documentType es obligatorio")
    @Pattern(
            regexp = "RESULTADO_LABORATORIO|IMAGEN_DIAGNOSTICA|REPORTE_PATOLOGIA|CONSENTIMIENTO|EXAMEN_EXTERNO|FOTO_CLINICA|ECG|OTRO",
            message = "documentType debe ser RESULTADO_LABORATORIO, IMAGEN_DIAGNOSTICA, REPORTE_PATOLOGIA, CONSENTIMIENTO, EXAMEN_EXTERNO, FOTO_CLINICA, ECG u OTRO"
    )
    private String documentType;

    @NotBlank(message = "fileName es obligatorio")
    @Size(max = 300, message = "fileName no puede exceder 300 caracteres")
    private String fileName;

    @Size(max = 500, message = "description no puede exceder 500 caracteres")
    private String description;

    @NotBlank(message = "fileUrl es obligatorio")
    @Size(max = 500, message = "fileUrl no puede exceder 500 caracteres")
    private String fileUrl;

    @Size(max = 100, message = "mimeType no puede exceder 100 caracteres")
    private String mimeType;

    @PositiveOrZero(message = "sizeBytes debe ser mayor o igual a 0")
    private Long sizeBytes;

    /** Profesional que carga el adjunto. Si no se envía, queda nulo. */
    private Long uploadingProfessionalId;

    /** Fecha del documento (no la de carga). */
    private LocalDate documentDate;

    private Boolean isConfidential;
}

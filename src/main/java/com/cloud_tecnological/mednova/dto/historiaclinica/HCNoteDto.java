package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HCNoteDto {

    /** "NOTA_ENFERMERIA" o "ATENCION_MEDICA". */
    private String source;
    private Long encounterId;
    private String noteType;
    private LocalDateTime noteAt;
    private String professionalName;
    private String contentPreview;
    private Boolean signed;
}

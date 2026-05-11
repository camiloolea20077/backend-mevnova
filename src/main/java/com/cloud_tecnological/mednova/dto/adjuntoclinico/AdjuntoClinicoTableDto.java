package com.cloud_tecnological.mednova.dto.adjuntoclinico;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdjuntoClinicoTableDto {

    private Long id;
    private Long patientId;
    private Long encounterId;
    private String documentType;
    private String fileName;
    private String mimeType;
    private Long sizeBytes;
    private LocalDate documentDate;
    private String uploadingProfessionalName;
    private Boolean isConfidential;
    private Boolean active;
    private LocalDateTime createdAt;
}

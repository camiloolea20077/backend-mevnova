package com.cloud_tecnological.mednova.dto.adjuntoclinico;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdjuntoClinicoResponseDto {

    private Long id;

    private Long patientId;
    private Long encounterId;

    private String documentType;
    private String fileName;
    private String description;
    private String fileUrl;
    private String mimeType;
    private Long sizeBytes;

    private Long uploadingProfessionalId;
    private String uploadingProfessionalName;

    private LocalDate documentDate;
    private Boolean isConfidential;

    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long updatedById;
}

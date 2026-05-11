package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HCAttachmentDto {

    private Long attachmentId;
    private String documentType;
    private String fileName;
    private String fileUrl;
    private LocalDate documentDate;
    private LocalDateTime createdAt;
    private Boolean isConfidential;
}

package com.cloud_tecnological.mednova.dto.notaenfermeria;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotaEnfermeriaTableDto {

    private Long id;
    private Long encounterId;
    private Long patientId;
    private String professionalName;
    private String noteType;
    private String shift;
    private LocalDateTime noteDate;
    private String contentPreview;
    private Boolean signed;
    private Boolean active;
}

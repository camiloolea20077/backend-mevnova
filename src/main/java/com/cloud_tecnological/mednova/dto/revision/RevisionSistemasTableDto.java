package com.cloud_tecnological.mednova.dto.revision;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RevisionSistemasTableDto {

    private Long id;
    private Long encounterId;
    private Long patientId;
    private String system;
    private Boolean withoutAlteration;
    private String findings;
    private Boolean active;
    private LocalDateTime createdAt;
}

package com.cloud_tecnological.mednova.dto.historiaclinica;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HCTimelineEventDto {

    /** ADMISION, ATENCION, NOTA, ORDEN, PRESCRIPCION, DISPENSACION, MAR, ESCALA, INTERCONSULTA, EPICRISIS, ADJUNTO. */
    private String eventType;
    private LocalDateTime eventAt;
    private Long referenceId;
    private String summary;
}

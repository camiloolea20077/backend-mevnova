package com.cloud_tecnological.mednova.dto.respuestaglosa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RespuestaGlosaResponseDto {

    private Long id;
    private Long glossId;
    private Long glossDetailId;
    private BigDecimal glossedValue;
    private String responseType;
    private BigDecimal acceptedValue;
    private String argumentation;
    private String supportUrl;
    private Long professionalId;
    private String professionalName;
    private LocalDateTime responseDate;
    private Boolean active;
    private LocalDateTime createdAt;
    private Boolean overdue;
}

package com.cloud_tecnological.mednova.dto.tarifario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TarifarioResponseDto {

    private Long id;
    private Long enterpriseId;
    private String code;
    private String name;
    private String description;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean active;
    private LocalDateTime createdAt;
}

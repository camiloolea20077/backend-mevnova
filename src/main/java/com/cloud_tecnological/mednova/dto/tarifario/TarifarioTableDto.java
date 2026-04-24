package com.cloud_tecnological.mednova.dto.tarifario;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TarifarioTableDto {

    private Long id;
    private String code;
    private String name;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean active;
}

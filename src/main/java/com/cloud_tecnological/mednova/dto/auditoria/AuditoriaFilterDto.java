package com.cloud_tecnological.mednova.dto.auditoria;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AuditoriaFilterDto {

    private String tableName;
    private Long userId;
    private String action;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String ipOrigin;
}

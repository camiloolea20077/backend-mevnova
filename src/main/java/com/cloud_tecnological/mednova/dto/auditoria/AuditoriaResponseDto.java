package com.cloud_tecnological.mednova.dto.auditoria;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AuditoriaResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long branchId;
    private Long userId;
    private String tableName;
    private String recordId;
    private String action;
    private String dataBefore;
    private String dataAfter;
    private String ipOrigin;
    private String userAgent;
    private LocalDateTime createdAt;
}

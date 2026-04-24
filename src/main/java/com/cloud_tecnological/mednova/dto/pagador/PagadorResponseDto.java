package com.cloud_tecnological.mednova.dto.pagador;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PagadorResponseDto {

    private Long id;
    private Long enterpriseId;
    private Long thirdPartyId;
    private String fullName;
    private String documentNumber;
    private String documentTypeCode;
    private String code;
    private Long payerTypeId;
    private String payerTypeName;
    private Long clientTypeId;
    private String clientTypeName;
    private String epsCode;
    private String administratorCode;
    private Integer filingDays;
    private Integer glossaResponseDays;
    private Boolean active;
    private LocalDateTime createdAt;
}

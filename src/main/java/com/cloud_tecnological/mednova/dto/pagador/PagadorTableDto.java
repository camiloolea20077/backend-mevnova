package com.cloud_tecnological.mednova.dto.pagador;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PagadorTableDto {

    private Long id;
    private Long thirdPartyId;
    private String fullName;
    private String documentNumber;
    private String documentTypeCode;
    private String code;
    private String payerTypeName;
    private Boolean active;
}

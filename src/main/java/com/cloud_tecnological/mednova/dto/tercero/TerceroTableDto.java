package com.cloud_tecnological.mednova.dto.tercero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TerceroTableDto {

    private Long id;
    private String documentTypeCode;
    private String documentNumber;
    private String fullName;
    private LocalDate birthDate;
    private String thirdPartyTypeName;
    private Boolean active;
}

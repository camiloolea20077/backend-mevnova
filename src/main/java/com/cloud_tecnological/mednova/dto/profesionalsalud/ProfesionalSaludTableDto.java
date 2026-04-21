package com.cloud_tecnological.mednova.dto.profesionalsalud;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfesionalSaludTableDto {

    private Long id;
    private Long thirdPartyId;
    private String fullName;
    private String documentTypeCode;
    private String documentNumber;
    private String medicalRegistrationNumber;
    private String primarySpecialtyName;
    private Boolean active;
}

package com.cloud_tecnological.mednova.dto.tercero;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TerceroResponseDto {

    private Long id;
    private Long enterpriseId;

    private Long thirdPartyTypeId;
    private String thirdPartyTypeName;

    private Long documentTypeId;
    private String documentTypeCode;
    private String documentTypeName;

    private String documentNumber;
    private String verificationDigit;

    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private String companyName;
    private String fullName;

    private LocalDate birthDate;

    private Long sexId;
    private Long genderId;
    private Long genderIdentityId;
    private Long sexualOrientationId;
    private Long maritalStatusId;
    private Long educationLevelId;
    private Long occupationId;
    private Long ethnicGroupId;
    private Long birthCountryId;
    private Long birthMunicipalityId;

    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

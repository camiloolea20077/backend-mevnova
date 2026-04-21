package com.cloud_tecnological.mednova.dto.tercero;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateTerceroRequestDto {

    @NotNull(message = "El tipo de tercero es obligatorio")
    private Long thirdPartyTypeId;

    @NotNull(message = "El tipo de documento es obligatorio")
    private Long documentTypeId;

    @NotBlank(message = "El número de documento es obligatorio")
    private String documentNumber;

    private String verificationDigit;

    private String firstName;

    private String secondName;

    private String firstLastName;

    private String secondLastName;

    private String companyName;

    @Past(message = "La fecha de nacimiento no puede ser futura")
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
}

package com.cloud_tecnological.mednova.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmpresaRequestDto {

    @NotBlank
    private String businessName;

    private String tradeName;

    private String legalRepresentative;

    private String phone;

    private String email;

    private Long countryId;

    private Long departmentId;

    private Long municipalityId;

    private String address;

    private String observations;

    private String logoUrl;
}

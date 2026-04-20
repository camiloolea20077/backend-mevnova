package com.cloud_tecnological.mednova.dto.empresa;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EmpresaResponseDto {

    private Long id;
    private String code;
    private String nit;
    private String verificationDigit;
    private String businessName;
    private String tradeName;
    private String legalRepresentative;
    private String phone;
    private String email;
    private Long countryId;
    private Long departmentId;
    private Long municipalityId;
    private String address;
    private String logoUrl;
    private String observations;
    private Boolean active;
    private LocalDateTime createdAt;
}

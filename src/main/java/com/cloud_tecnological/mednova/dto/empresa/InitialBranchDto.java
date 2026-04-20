package com.cloud_tecnological.mednova.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitialBranchDto {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String repsCode;

    @NotNull
    private Long countryId;

    @NotNull
    private Long departmentId;

    @NotNull
    private Long municipalityId;

    private String address;

    private String phone;

    private String email;
}

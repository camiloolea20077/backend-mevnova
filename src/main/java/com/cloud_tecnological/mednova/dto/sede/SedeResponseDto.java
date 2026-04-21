package com.cloud_tecnological.mednova.dto.sede;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SedeResponseDto {

    private Long id;
    private String code;
    private String repsCode;
    private String name;
    private Long countryId;
    private Long departmentId;
    private Long municipalityId;
    private String municipalityName;
    private String address;
    private String phone;
    private String email;
    private Boolean isPrincipal;
    private Boolean active;
    private LocalDateTime createdAt;
}
